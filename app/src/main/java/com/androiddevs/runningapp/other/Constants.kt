package com.androiddevs.runningapp.other

import android.graphics.Color
import android.location.LocationManager
import com.github.mikephil.charting.data.LineDataSet

class Constants {

    companion object {
        const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

        const val LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER
        const val MIN_LOCATION_UPDATE_INTERVAL = 0L
        const val MIN_LOCATION_UPDATE_DISTANCE = 0f

        const val POLYLINE_COLOR = Color.RED
        const val POLYLINE_WIDTH = 8f
        const val MAP_ZOOM = 15f

        const val TIMER_UPDATE_INTERVAL = 50L

        val LINE_DATA_MODE = LineDataSet.Mode.CUBIC_BEZIER

        const val MAP_VIEW_HEIGHT_IN_DP = 200f

        // Shared Preferences
        const val SHARED_PREFERENCES_NAME = "sharedPref"
        const val KEY_NAME = "KEY_NAME"
        const val KEY_WEIGHT = "KEY_WEIGHT"
        const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"

        const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    }
}