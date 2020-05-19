package com.androiddevs.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.other.Constants.Companion.LOCATION_PROVIDER
import com.androiddevs.runningapp.other.Constants.Companion.MAP_VIEW_BUNDLE_KEY
import com.androiddevs.runningapp.other.Constants.Companion.MAP_ZOOM
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_DISTANCE
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningapp.other.Constants.Companion.POLYLINE_COLOR
import com.androiddevs.runningapp.other.Constants.Companion.POLYLINE_WIDTH
import com.androiddevs.runningapp.other.TrackingUtility
import com.androiddevs.runningapp.ui.HomeActivity
import com.androiddevs.runningapp.ui.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber

class TrackingFragment : BaseFragment(R.layout.fragment_tracking), LocationListener {

    var map: GoogleMap? = null

    var isTracking = false
    private var pathPoints = mutableListOf<LatLng>()

    private lateinit var viewModel: TrackingViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        viewModel = (activity as HomeActivity).trackingViewModel

        viewModel.isTracking.observe(viewLifecycleOwner, Observer {
            isTracking = it
            Timber.d("IsTracking is now $isTracking")
            updateLocationChecking()
        })

        viewModel.pathPoints.observe(viewLifecycleOwner, Observer {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(it)
            map?.addPolyline(polylineOptions)
            if(it.isNotEmpty()) {
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.last(), MAP_ZOOM))
            }
        })

        viewModel.timeRunInSeconds.observe(viewLifecycleOwner, Observer {
            val formattedTime = TrackingUtility.getFormattedTimeWithSeconds(it)
            tvTimer.text = formattedTime
        })

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mapView.getMapAsync {
            map = it.apply {
                setMinZoomPreference(MAP_ZOOM)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationChecking() {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(requireContext())) {
                locationManager.requestLocationUpdates(
                    LOCATION_PROVIDER,
                    MIN_LOCATION_UPDATE_INTERVAL,
                    MIN_LOCATION_UPDATE_DISTANCE,
                    this
                )
                btnToggleRun.text = "Stop"
                Timber.d("Tracking started")
            } else {
                Snackbar.make(
                    requireView(),
                    "Please accept the location permissions first",
                    Snackbar.LENGTH_LONG
                ).show()
            }

        } else {
            locationManager.removeUpdates(this)
            btnToggleRun.text = "Start"
            Timber.d("Tracking stopped")
        }
    }

    override fun onLocationChanged(newLocation: Location?) {
        if (isTracking) {
            addPathPoint(newLocation)
            Timber.d("Location changed: (${newLocation?.latitude}, ${newLocation?.longitude})")
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.d("Status changed: $status")
    }

    override fun onProviderEnabled(provider: String?) {
        Timber.d("Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String?) {
        Timber.d("Provider disabled: $provider")
    }

    @SuppressLint("MissingPermission")
    private fun toggleRun() {
        viewModel.toggleRun()
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            if (pathPoints.isEmpty()) {
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, MAP_ZOOM))
            }

            viewModel.addPathPoint(pos)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapViewBundle?.let {
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, Bundle())
            Timber.d("Putting empty Bundle")
        } ?: mapView.onSaveInstanceState(mapViewBundle).also {
            Timber.d("Putting non-empty bundle")
        }
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}