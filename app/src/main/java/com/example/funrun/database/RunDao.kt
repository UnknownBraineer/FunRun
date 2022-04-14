package com.example.funrun.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface RunDao {

    // Basic suspend functions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: Run)
    @Delete
    suspend fun delete(run: Run)

    // Filter functions
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getRunsByDate(): LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getRunsByTimeInMillis(): LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getRunsByCaloriesBurned(): LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getRunsByAvgSpeed(): LiveData<List<Run>>
    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getRunsByDistance(): LiveData<List<Run>>


    // Functions for Statistics fragment
    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis(): LiveData<Long>
    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>
    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistance(): LiveData<Int>
    @Query("SELECT AVG(avgSpeedInKMH) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>

}