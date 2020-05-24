package com.androiddevs.runningapp.ui.fragments

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.adapters.RunAdapter
import com.androiddevs.runningapp.db.Run
import com.androiddevs.runningapp.other.SortType
import com.androiddevs.runningapp.ui.HomeActivity
import com.androiddevs.runningapp.ui.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

private const val REQUEST_CODE_LOCATION_PERMISSION = 0

class RunFragment : BaseFragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    @Inject
    lateinit var runAdapter: RunAdapter

    private lateinit var viewModel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as HomeActivity).homeViewModel
        setupRecyclerView()
        requestPermissions()
        fab.setOnClickListener {
            //findNavController().navigate(R.id.action_runFragment2_to_trackingFragment)
            Intent(requireContext(), TrackingActivity::class.java).also {
                startActivity(it)
            }
        }

        viewModel.runs.observe(viewLifecycleOwner, Observer { runs ->
            runAdapter.submitList(runs)
        })

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> { // Date
                        viewModel.filterRuns(SortType.DATE)
                    }
                    1 -> { // Running Time
                        viewModel.filterRuns(SortType.RUNNING_TIME)
                    }
                    2 -> { // Distance
                        viewModel.filterRuns(SortType.DISTANCE)
                    }
                    3 -> { // Average Speed
                        viewModel.filterRuns(SortType.AVG_SPEED)
                    }
                    4 -> { // Calories Burned
                        viewModel.filterRuns(SortType.CALORIES_BURNED)
                    }
                }
            }
        }
    }

    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val run = runAdapter.differ.currentList[position]
            viewModel.deleteRun(run)
            Snackbar.make(requireView(), "Successfully deleted run", Snackbar.LENGTH_LONG).apply {
                setAction("Undo") {
                    viewModel.insertRun(run)
                }
                show()
            }
        }
    }

    private fun setupRecyclerView() = rvRuns.apply {
        adapter = runAdapter
        layoutManager = LinearLayoutManager(activity)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                *permissions
            )
        } else {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            if (!EasyPermissions.hasPermissions(requireContext(), *permissions)) {
                EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}