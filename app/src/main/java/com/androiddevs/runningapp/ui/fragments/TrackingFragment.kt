package com.androiddevs.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.Observer
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.db.RunDao
import com.androiddevs.runningapp.other.Constants.Companion.MAP_VIEW_BUNDLE_KEY
import com.androiddevs.runningapp.other.Constants.Companion.MAP_ZOOM
import com.androiddevs.runningapp.other.Constants.Companion.POLYLINE_COLOR
import com.androiddevs.runningapp.other.Constants.Companion.POLYLINE_WIDTH
import com.androiddevs.runningapp.other.MapPoint
import com.androiddevs.runningapp.other.TrackingUtility
import com.androiddevs.runningapp.services.ACTION_PAUSE_SERVICE
import com.androiddevs.runningapp.services.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.runningapp.services.ACTION_STOP_SERVICE
import com.androiddevs.runningapp.services.TrackingService
import com.androiddevs.runningapp.ui.HomeActivity
import com.androiddevs.runningapp.ui.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

class TrackingFragment : BaseFragment(R.layout.fragment_tracking) {

    @Inject
    lateinit var runDao: RunDao

    private var map: GoogleMap? = null

    private var isTracking = false
    private var curTimeInMillis = 0L
    private var pathPoints = mutableListOf<MutableList<LatLng>>()

    private lateinit var viewModel: TrackingViewModel

    private var trackingService: TrackingService? = null
    private var trackingBinder: TrackingService.TrackingBinder? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        viewModel = (activity as HomeActivity).trackingViewModel

        subscribeToObservers()

        viewModel.trackingBinder.observe(viewLifecycleOwner, Observer { trackingBinder ->
            this@TrackingFragment.trackingBinder = trackingBinder
            trackingService = trackingBinder.service
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

    private fun subscribeToObservers() {
        viewModel.isTracking.observe(viewLifecycleOwner, Observer {
            isTracking = it
            if (curTimeInMillis > 0L && !isTracking) {
                btnToggleRun.text = getString(R.string.start_text)
                btnFinishRun.visibility = View.VISIBLE
            } else if(isTracking){
                btnToggleRun.text = getString(R.string.stop_text)
                btnFinishRun.visibility = View.GONE
            }
        })

        viewModel.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            for(polyline in it) {
                val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)

                map?.addPolyline(polylineOptions)
            }

            if (it.isNotEmpty() && it.last().isNotEmpty()) {
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(it.last().last(), MAP_ZOOM))
            }
        })

        viewModel.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTimeWithMillis(it)
            tvTimer.text = formattedTime
        })
    }

    @SuppressLint("MissingPermission")
    private fun toggleRun() {
        if(isTracking) {
            btnToggleRun.text = getString(R.string.stop_text)
            pauseTrackingService()
        } else {
            btnToggleRun.text = getString(R.string.start_text)
            startOrResumeTrackingService()
        }
    }

    private fun startOrResumeTrackingService() = Intent(requireActivity(), TrackingService::class.java).also {
        it.action = ACTION_START_OR_RESUME_SERVICE
        requireActivity().applicationContext.startService(it)
        bindService()
    }

    private fun pauseTrackingService() = Intent(requireActivity(), TrackingService::class.java).also {
        it.action = ACTION_PAUSE_SERVICE
        requireActivity().applicationContext.startService(it)
    }

    private fun stopTrackingService() = Intent(requireContext(), TrackingService::class.java).also {
        it.action = ACTION_STOP_SERVICE
        requireActivity().applicationContext.startService(it)
    }

    private fun bindService() {
        val serviceBindIntent = Intent(requireActivity(), TrackingService::class.java)
        requireContext().applicationContext.bindService(serviceBindIntent, viewModel.serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*savedInstanceState?.let { state ->
            Timber.d("SavedInstanceState is not null")
            if(state.containsKey("isTracking")) {
                isTracking = state.getBoolean("isTracking")
                Timber.d("Updated isTracking with $isTracking")
            }
            if(state.containsKey("curTimeInMillis")) {
                curTimeInMillis = state.getLong("curTimeInMillis")
            }
            if(state.containsKey("binder")) {
                trackingBinder = state.getBinder("binder") as TrackingService.TrackingBinder
            }
            if(state.containsKey("pathPoints")) {
                pathPoints = state.getParcelableArray("pathPoints")?.map {
                    val curPoint = (it as MapPoint)
                    LatLng(curPoint.latitude, curPoint.longitude)
                }?.toMutableList() ?: return
            }
        } ?: Timber.d("SavedInstanceState is null")*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        /*outState.putBoolean("isTracking", isTracking)
        outState.putBinder("binder", trackingBinder)
        outState.putLong("curTimeInMillis", curTimeInMillis)
        val mapPointArray = pathPoints.map { MapPoint(it.latitude, it.longitude) }.toTypedArray()
        outState.putParcelableArray("pathPoints", mapPointArray)
        Timber.d("PUT SOMETHING IN SAVED INSTANCE STATE")
        arguments?.putBundle("savedState", outState)
        super.onSaveInstanceState(outState)*/

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
        for (polyline in pathPoints) {
            for(point in polyline) {
                bounds.include(point)
            }
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
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculateTotalDistance(polyline).toInt()
            }
            Timber.d("curTimeInMillis: $curTimeInMillis")
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val date = Calendar.getInstance().time
            val weight = requireContext().getSharedPreferences("sharedPref", MODE_PRIVATE).getFloat("weight", 80f)
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
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

    override fun onDestroy() {
        super.onDestroy()
        //requireActivity().unbindService(viewModel.serviceConnection)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}