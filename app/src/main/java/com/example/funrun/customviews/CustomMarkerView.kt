package com.example.funrun.customviews

import android.content.Context
import android.view.LayoutInflater
import com.example.funrun.database.Run
import com.example.funrun.databinding.MarkerViewBinding
import com.example.funrun.utils.getFormattedStopWatchTime
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    context: Context,
    layoutId: Int
) : MarkerView(context, layoutId) {

    private lateinit var binding: MarkerViewBinding

    init {
        binding = MarkerViewBinding.inflate(LayoutInflater.from(context), this, false)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)
        binding.tvAvgSpeed.text = "${run.avgSpeedInKMH}km/h"
        binding.tvDistance.text = "${run.distanceInMeters / 1000f}km"
        binding.tvDuration.text = getFormattedStopWatchTime(run.timeInMillis)
        binding.tvCaloriesBurned.text = "${run.caloriesBurned}kcal"
    }

}