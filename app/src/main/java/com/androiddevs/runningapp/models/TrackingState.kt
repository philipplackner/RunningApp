package com.androiddevs.runningapp.models

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline

data class TrackingState(
    var isTracking: Boolean,
    var pathPoints: MutableList<LatLng>
)