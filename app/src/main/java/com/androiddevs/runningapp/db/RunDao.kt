package com.androiddevs.runningapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY date")
    fun getAllRuns(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMin")
    fun getAllRunsSortedByTimeInMin(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distance")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeed")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

}