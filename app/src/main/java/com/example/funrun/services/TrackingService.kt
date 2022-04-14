package com.example.funrun.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.funrun.R
import com.example.funrun.ui.MainActivity
import com.example.funrun.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.funrun.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.funrun.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.funrun.utils.Constants.ACTION_STOP_SERVICE
import com.example.funrun.utils.Constants.FASTEST_LOCATION_INTERVAL
import com.example.funrun.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.funrun.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.funrun.utils.Constants.NOTIFICATION_ID
import com.example.funrun.utils.Constants.TIMER_UPDATE_INTERVAL
import com.example.funrun.utils.createNotificationChannel
import com.example.funrun.utils.getFormattedStopWatchTime
import com.example.funrun.utils.hasLocalPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false


    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        private val _areWeTracking = MutableLiveData<Boolean>()
        val areWeTracking: LiveData<Boolean>
            get() = _areWeTracking
        private val _pathPoints = MutableLiveData<Polylines>()
        val pathPoints: LiveData<Polylines>
            get() = _pathPoints
        private val _timeRunInMillis = MutableLiveData<Long>()
        val timeRunInMillis: LiveData<Long>
            get() = _timeRunInMillis
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (areWeTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("New Location: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            _pathPoints.value?.apply {
                last().add(pos)
                _pathPoints.postValue(this)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitValue()
        areWeTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun postInitValue() {
        _areWeTracking.postValue(false)
        _pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        _timeRunInMillis.postValue(0L)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(areWeTracking: Boolean) {
        if (areWeTracking) {
            if (hasLocalPermissions(this)) {
                val request = com.google.android.gms.location.LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun updateNotificationTrackingState(areWeTracking: Boolean) {
        val notificationActionText = if (areWeTracking) "Pause" else "Resume"
        val pendingIntent = if (areWeTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,currentNotificationBuilder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused the service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped the service")
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun startForegroundService() {
        startTimer()
        _areWeTracking.postValue(true)

        // Notification setup
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel()
        }

        timeRunInSeconds.observe(this, Observer {
            if (!serviceKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
    }

    private fun startTimer() {
        addEmptyPolyline()
        _areWeTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (areWeTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                _timeRunInMillis.postValue(timeRun + lapTime)
                if (_timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }

    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L



    private fun pauseService() {
        _areWeTracking.postValue(false)
        isTimerEnabled = false
    }





    private fun addEmptyPolyline() = _pathPoints.value?.apply {
        add(mutableListOf())
        _pathPoints.postValue(this)
    } ?: _pathPoints.postValue(mutableListOf(mutableListOf()))



    private fun getIntent(action: String) = Intent(
        this,
        MainActivity::class.java
    ).also {
        it.action = action
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitValue()
        stopForeground(true)
        stopSelf()
    }



}