package com.androiddevs.runningapp.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.androiddevs.runningapp.services.Polyline
import com.google.android.gms.maps.model.LatLng
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

class TrackingUtility {

    companion object {
        /**
         * Checks if the user accepted the necessary location permissions or not
         */
        fun hasLocationPermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }

        /**
         * Takes an amount of milliseconds and converts it to a formatted string, optionally
         * with milliseconds
         */
        fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
            var milliseconds = ms
            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
            milliseconds -= TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
            if (!includeMillis) {
                return "${if (hours < 10) "0" else ""}$hours:" +
                        "${if (minutes < 10) "0" else ""}$minutes:" +
                        "${if (seconds < 10) "0" else ""}$seconds"
            }
            milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
            milliseconds /= 10
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds:" +
                    "${if (milliseconds < 10) "0" else ""}$milliseconds"
        }

        /**
         * Calculates the length of a specific polyline
         */
        fun calculatePolylineLength(polyline: Polyline): Float {
            var distance = 0f
            for (i in 0..polyline.size - 2) {
                val pos1 = polyline[i]
                val pos2 = polyline[i + 1]
                val result = FloatArray(1)
                Location.distanceBetween(
                    pos1.latitude,
                    pos1.longitude,
                    pos2.latitude,
                    pos2.longitude,
                    result
                )
                distance += result[0]
            }
            return distance
        }
    }
}