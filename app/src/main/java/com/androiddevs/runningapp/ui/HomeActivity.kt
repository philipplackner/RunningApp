package com.androiddevs.runningapp.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.di.ViewModelProviderFactory
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

class HomeActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProvider.Factory

    lateinit var trackingViewModel: TrackingViewModel
    lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        trackingViewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(TrackingViewModel::class.java)
        homeViewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(HomeViewModel::class.java)

        Navigation.findNavController(this, R.id.navHostFragment)
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.setupFragment2, R.id.trackingFragment -> bottomNavigationView.visibility =
                        View.GONE
                    else -> bottomNavigationView.visibility = View.VISIBLE
                }
            }
    }
}