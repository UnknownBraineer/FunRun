package com.example.funrun.repositories

import androidx.lifecycle.LiveData
import com.example.funrun.database.Run
import com.example.funrun.database.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao: RunDao
) {

    // Basic suspend functions
    suspend fun insert(run: Run) = runDao.insert(run)
    suspend fun delete(run: Run) = runDao.delete(run)



    // Filter Functions
    fun getRunsByDate() = runDao.getRunsByDate()
    fun getRunsByTimeInMillis() = runDao.getRunsByTimeInMillis()
    fun getRunsByCaloriesBurned() = runDao.getRunsByCaloriesBurned()
    fun getRunsByAvgSpeed() = runDao.getRunsByAvgSpeed()
    fun getRunsByDistance() = runDao.getRunsByDistance()

    // Functions for Statistics fragment
    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()
    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()
    fun getTotalDistance() = runDao.getTotalDistance()

}