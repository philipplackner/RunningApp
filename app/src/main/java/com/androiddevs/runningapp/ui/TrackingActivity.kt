package com.androiddevs.runningapp.ui

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.androiddevs.runningapp.R
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class TrackingActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProvider.Factory
    lateinit var trackingViewModel: TrackingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackingViewModel = ViewModelProvider(this, viewModelProviderFactory).get(TrackingViewModel::class.java)
        setContentView(R.layout.activity_tracking)

    }
}
