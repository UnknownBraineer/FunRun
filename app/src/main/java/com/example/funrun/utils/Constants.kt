package com.example.funrun.utils

import android.graphics.Color

object Constants {

    const val DATABASE_NAME = "run_db"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    // Service constants
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    // Notification constants
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    // Pending Intent
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    // TRACKING INTERVAL
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    // Polyline Settings
    const val POLYLINE_COLOR = Color.GREEN
    const val POLYLINE_WIDTH = 8F

    // Camera setting
    const val MAP_ZOOM = 15F

    // Coroutine constants
    const val TIMER_UPDATE_INTERVAL = 50L

    // SharedPref consts
    const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
}