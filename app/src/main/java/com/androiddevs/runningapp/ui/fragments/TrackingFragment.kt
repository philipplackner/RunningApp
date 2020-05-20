package com.androiddevs.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.db.RunDao
import com.androiddevs.runningapp.db.RunningDatabase
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_tracking.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

class TrackingFragment : BaseFragment(R.layout.fragment_tracking), LocationListener {

    @Inject
    lateinit var runDao: RunDao

    private var map: GoogleMap? = null

    private var isTracking = false
    private var curTimeInMillis = 0L
    private var pathPoints = mutableListOf<LatLng>()

    private lateinit var viewModel: TrackingViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        viewModel = (activity as HomeActivity).trackingViewModel

        viewModel.isTracking.observe(viewLifecycleOwner, Observer {
            isTracking = it
            if (curTimeInMillis > 0L && !isTracking) {
                btnFinishRun.visibility = View.VISIBLE
            } else {
                btnFinishRun.visibility = View.GONE
            }
            Timber.d("IsTracking is now $isTracking")
            updateLocationChecking()
        })

        viewModel.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(it)
            map?.addPolyline(polylineOptions)
            if (it.isNotEmpty()) {
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.last(), MAP_ZOOM))
            }
        })

        viewModel.timeRunInSeconds.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedTimeWithSeconds(it)
            tvTimer.text = formattedTime
        })

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToWholeDistance()
            saveRunToDB()
        }

        mapView.getMapAsync {
            map = it
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

    private fun zoomToWholeDistance() {
        val bounds = LatLngBounds.Builder()
        for (point in pathPoints) {
            bounds.include(point)
        }
        val width = mapView.width
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                width,
                height,
                (height * 0.05f).toInt()
            )
        )
    }

    private fun saveRunToDB() {
        map?.snapshot { bmp ->
            val distanceInMeters = TrackingUtility.calculateTotalDistance(pathPoints).toInt()
            Timber.d("distanceInMeters: $distanceInMeters")
            Timber.d("curTimeInMillis: $curTimeInMillis")
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val date = Calendar.getInstance().time
            val weight = activity?.getSharedPreferences("sharedPref", MODE_PRIVATE)?.getFloat("weight", 80f)
            val caloriesBurned = ((distanceInMeters / 1000f) * weight!!).toInt()
            val run = Run(bmp, date, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(requireView(), "Run saved successfully.", Snackbar.LENGTH_LONG).show()
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