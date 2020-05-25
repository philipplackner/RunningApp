package com.androiddevs.runningapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.other.Constants.Companion.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import timber.log.Timber
import javax.inject.Inject

class HomeActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProvider.Factory

    lateinit var homeViewModel: HomeViewModel
    lateinit var statisticsViewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        homeViewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(HomeViewModel::class.java)
        statisticsViewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(StatisticsViewModel::class.java)

        navigateToTrackingFragmentIfNeeded(intent)

        Navigation.findNavController(this, R.id.navHostFragment)
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.setupFragment2, R.id.trackingFragment -> bottomNavigationView.visibility =
                        View.GONE
                    else -> bottomNavigationView.visibility = View.VISIBLE
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }

}