package com.example.funrun.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.funrun.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val repository: MainRepository
) : ViewModel() {

    val totalTimeRun = repository.getTotalTimeInMillis()
    val totalDistance = repository.getTotalDistance()
    val totalAvgSpeed = repository.getTotalAvgSpeed()
    val totalCaloriesBurned = repository.getTotalCaloriesBurned()

    val runsSortedByDate = repository.getRunsByDate()

}