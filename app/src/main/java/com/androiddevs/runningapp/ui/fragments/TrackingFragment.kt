package com.androiddevs.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.db.RunDao
import com.androiddevs.runningapp.other.Constants.Companion.MAP_VIEW_BUNDLE_KEY
import com.androiddevs.runningapp.other.Constants.Companion.MAP_ZOOM
import com.androiddevs.runningapp.other.Constants.Companion.POLYLINE_COLOR
import com.androiddevs.runningapp.other.Constants.Companion.POLYLINE_WIDTH
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
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
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

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapViewBundle = savedInstanceState?.getBundle(MAP_VIEW_BUNDLE_KEY)
        mapView.onCreate(mapViewBundle)
        viewModel = (activity as HomeActivity).trackingViewModel

        subscribeToObservers()

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToWholeDistance()
            saveRunToDB()
            stopRun()
        }

        mapView.getMapAsync {
            map = it
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            isTracking = it
            if (curTimeInMillis > 0L && !isTracking) {
                btnToggleRun.text = getString(R.string.start_text)
                btnFinishRun.visibility = View.VISIBLE
            } else if(isTracking){
                btnToggleRun.text = getString(R.string.stop_text)
                menu?.getItem(0)?.isVisible = true
                btnFinishRun.visibility = View.GONE
            }
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
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

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTimeWithMillis(it)
            tvTimer.text = formattedTime
        })
    }

    @SuppressLint("MissingPermission")
    private fun toggleRun() {
        if(isTracking) {
            btnToggleRun.text = getString(R.string.stop_text)
            menu?.getItem(0)?.isVisible = true
            pauseTrackingService()
        } else {
            btnToggleRun.text = getString(R.string.start_text)
            startOrResumeTrackingService()
            Timber.d("Started service")
        }
    }

    private fun startOrResumeTrackingService() = Intent(requireContext().applicationContext, TrackingService::class.java).also {
        it.action = ACTION_START_OR_RESUME_SERVICE
        requireContext().applicationContext.startService(it)
    }

    private fun pauseTrackingService() = Intent(requireContext().applicationContext, TrackingService::class.java).also {
        it.action = ACTION_PAUSE_SERVICE
        requireContext().applicationContext.startService(it)
    }

    private fun stopTrackingService() = Intent(requireContext().applicationContext, TrackingService::class.java).also {
        it.action = ACTION_STOP_SERVICE
        requireContext().applicationContext.startService(it)
    }


    override fun onSaveInstanceState(outState: Bundle) {
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
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val date = Calendar.getInstance().time
            val weight = requireContext().getSharedPreferences("sharedPref", MODE_PRIVATE).getFloat("weight", 80f)
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, date, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView), "Run saved successfully.", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun stopRun() {
        stopTrackingService()
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment2)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu_tracking, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // just checking for isTracking doesnt trigger this when rotating the device
        // in paused mode
        if(curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure that you want to cancel the current run and delete its data?")
            .setIcon(R.drawable.ic_delete_black_24dp)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
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