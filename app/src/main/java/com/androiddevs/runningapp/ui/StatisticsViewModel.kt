package com.androiddevs.runningapp.ui

import androidx.lifecycle.ViewModel
import com.androiddevs.runningapp.repositories.HomeRepository
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    homeRepository: HomeRepository
) : ViewModel() {

    var totalDistance = homeRepository.getTotalDistance()
    var totalTimeInMillis = homeRepository.getTotalTimeInMillis()
    var totalAvgSpeed = homeRepository.getTotalAvgSpeed()
    var totalCaloriesBurned = homeRepository.getTotalCaloriesBurned()

    var runsSortedByDate = homeRepository.getAllRunsSortedByDate()
}