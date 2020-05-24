package com.androiddevs.runningapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.repositories.HomeRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import javax.inject.Inject

class TrackingViewModel @Inject constructor(
    val homeRepository: HomeRepository
) : ViewModel() {

    fun insertRun(run: Run) = viewModelScope.launch {
        homeRepository.insertRun(run)
    }

}