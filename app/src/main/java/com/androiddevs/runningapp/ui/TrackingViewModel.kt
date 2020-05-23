package com.androiddevs.runningapp.ui

import android.content.ComponentName
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.other.Constants.Companion.TIMER_UPDATE_INTERVAL
import com.androiddevs.runningapp.repositories.HomeRepository
import com.androiddevs.runningapp.services.TrackingService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TrackingViewModel @Inject constructor(
    val homeRepository: HomeRepository
) : ViewModel() {

    val trackingBinder = MutableLiveData<TrackingService.TrackingBinder>()

    val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            Timber.d("$componentName disconnected from service.")
            trackingBinder.postValue(null)
        }

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            Timber.d("$componentName connected to service")
            trackingBinder.postValue(binder as TrackingService.TrackingBinder)
        }
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        homeRepository.insertRun(run)
    }

}