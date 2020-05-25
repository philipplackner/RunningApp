package com.androiddevs.runningapp.ui.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.other.DateValueFormatter
import com.androiddevs.runningapp.other.TrackingUtility
import com.androiddevs.runningapp.ui.HomeActivity
import com.androiddevs.runningapp.ui.StatisticsViewModel
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

class StatisticsFragment : BaseFragment(R.layout.fragment_statistics) {

    lateinit var viewModel: StatisticsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as HomeActivity).statisticsViewModel
        setupLineChart()
        subscribeToObservers()
    }

    private fun setupLineChart() {
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = DateValueFormatter()
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        lineChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        lineChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        lineChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
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
            val roundedAvgSpeed = round(it * 10f) / 10f
            val totalAvgSpeed = "${roundedAvgSpeed}km/h"
            tvAverageSpeed.text = totalAvgSpeed
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            val totalCaloriesBurned = "${it}kcal"
            tvTotalCalories.text = totalCaloriesBurned
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            val allAvgSpeeds = mutableListOf<Entry>()
            for(run in it) {
                allAvgSpeeds.add(Entry(run.timestamp.toFloat(), run.avgSpeedInKMH))
            }
            val lineDataSet = LineDataSet(allAvgSpeeds, "Avg Speed over Time")
            lineDataSet.valueTextColor = Color.WHITE
            lineDataSet.color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
            lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            val lineData = LineData(lineDataSet)
            lineChart.data = lineData
            lineChart.invalidate()
        })
    }
}