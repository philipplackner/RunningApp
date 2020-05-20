package com.androiddevs.runningapp.ui

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.other.Constants.Companion.TIMER_UPDATE_INTERVAL
import com.androiddevs.runningapp.repositories.HomeRepository
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrackingViewModel @Inject constructor(
    val homeRepository: HomeRepository
) : ViewModel() {

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L

    val isTracking = MutableLiveData<Boolean>()
    val pathPoints = MutableLiveData<MutableList<LatLng>>()
    val timeRunInSeconds = MutableLiveData<Long>()

    init {
        timeRunInSeconds.postValue(0L)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    fun toggleRun() {
        val newIsTracking = !isTracking.value!!
        isTracking.postValue(newIsTracking)
        if (newIsTracking) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        viewModelScope.launch {
            while (isTimerEnabled) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInSeconds.postValue(timeRun + lapTime)
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun stopTimer() {
        isTimerEnabled = false
    }

    fun addPathPoint(pos: LatLng) {
        pathPoints.value?.apply {
            add(pos)
            pathPoints.postValue(this)
        }
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        homeRepository.insertRun(run)
    }

}