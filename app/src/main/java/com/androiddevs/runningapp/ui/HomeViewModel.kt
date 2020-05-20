package com.androiddevs.runningapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.repositories.HomeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    val homeRepository: HomeRepository
) : ViewModel() {

    fun getAllRunsSortedByDistance() = homeRepository.getAllRunsSortedByDistance()

    fun insertRun(run: Run) = viewModelScope.launch {
        homeRepository.insertRun(run)
    }

    fun deleteRun(run: Run) = viewModelScope.launch {
        homeRepository.deleteRun(run)
    }
}