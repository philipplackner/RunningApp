package com.androiddevs.runningapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androiddevs.runningapp.models.TrackingState
import com.androiddevs.runningapp.other.Constants
import com.androiddevs.runningapp.other.Constants.Companion.LOCATION_PROVIDER
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_DISTANCE
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningapp.other.LocationUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber

class TrackingViewModel: ViewModel() {

    val isTracking = MutableLiveData<Boolean>()
    val pathPoints = MutableLiveData<MutableList<LatLng>>()

    fun initLiveData() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    @SuppressLint("MissingPermission")
    fun toggleRun() {
        isTracking.postValue(!isTracking.value!!)
    }

    fun addPathPoint(pos: LatLng) {
        val newPathPoints = pathPoints.value
        newPathPoints?.add(pos)
        pathPoints.postValue(newPathPoints)
    }

}