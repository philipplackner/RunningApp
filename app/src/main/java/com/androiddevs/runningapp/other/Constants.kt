package com.androiddevs.runningapp.other

import android.graphics.Color
import android.location.LocationManager

class Constants {

    companion object {
        const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

        const val LOCATION_PROVIDER = LocationManager.GPS_PROVIDER
        const val MIN_LOCATION_UPDATE_INTERVAL = 0L
        const val MIN_LOCATION_UPDATE_DISTANCE = 5f

        const val POLYLINE_COLOR = Color.RED
        const val POLYLINE_WIDTH = 8f
        const val MAP_ZOOM = 15f

        const val TIMER_UPDATE_INTERVAL = 50L
    }
}