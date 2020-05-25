package com.androiddevs.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.other.TrackingUtility
import com.androiddevs.runningapp.ui.HomeActivity
import com.androiddevs.runningapp.ui.HomeViewModel
import com.androiddevs.runningapp.ui.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

class StatisticsFragment : BaseFragment(R.layout.fragment_statistics) {

    lateinit var viewModel: StatisticsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as HomeActivity).statisticsViewModel
        subscribeToObservers()

        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
        }
    }

    private fun subscribeToObservers() {
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            val km = it / 1000f
            val totalDistance = round(km * 10) / 10f
            val totalDistanceString = "${totalDistance}km"
            tvTotalDistance.text = totalDistanceString
        })

        viewModel.totalTimeInMillis.observe(viewLifecycleOwner, Observer {
            val totalTimeInMillis = TrackingUtility.getFormattedPreviewTimeWithMillis(it)
            tvTotalTime.text = totalTimeInMillis
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            val totalAvgSpeed = "${it}km/h"
            tvAverageSpeed.text = totalAvgSpeed
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            val totalCaloriesBurned = "${it}kcal"
            tvTotalCalories.text = totalCaloriesBurned
        })
    }
}