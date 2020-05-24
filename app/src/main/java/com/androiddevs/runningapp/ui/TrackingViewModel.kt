package com.androiddevs.runningapp.ui

import android.content.ComponentName
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.lifecycle.*
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.other.Constants.Companion.TIMER_UPDATE_INTERVAL
import com.androiddevs.runningapp.repositories.HomeRepository
import com.androiddevs.runningapp.services.TrackingService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TrackingViewModel @Inject constructor(
    val homeRepository: HomeRepository
) : ViewModel() {

    var timeRunInMillis = MutableLiveData<Long>()
    var isTracking = MutableLiveData<Boolean>()
    var pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()

    val trackingBinder = MutableLiveData<TrackingService.TrackingBinder>()

    init {
        timeRunInMillis.postValue(0L)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            Timber.d("$componentName disconnected from service.")
            trackingBinder.postValue(null)
            timeRunInMillis.postValue(0L)
            isTracking.postValue(false)
            pathPoints.postValue(mutableListOf())
        }

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            Timber.d("$componentName connected to service")
            val newBinder = binder as TrackingService.TrackingBinder
            Timber.d(newBinder.hashCode().toString())
            trackingBinder.postValue(newBinder)
            newBinder.service.timeRunInMillis.observeForever {
                timeRunInMillis.postValue(it)
            }
            newBinder.service.isTracking.observeForever {
                isTracking.postValue(it)
            }
            newBinder.service.pathPoints.observeForever {
                pathPoints.postValue(it)
            }
        }
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        homeRepository.insertRun(run)
    }

}