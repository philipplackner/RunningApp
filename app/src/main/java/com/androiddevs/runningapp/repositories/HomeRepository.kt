package com.androiddevs.runningapp.repositories

import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.db.RunDao
import javax.inject.Inject

class HomeRepository @Inject constructor(
    val runDao: RunDao
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByTimeInMin() = runDao.getAllRunsSortedByTimeInMin()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()
}