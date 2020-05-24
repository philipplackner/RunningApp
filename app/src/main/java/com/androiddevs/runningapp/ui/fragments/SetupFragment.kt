package com.androiddevs.runningapp.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.other.Constants.Companion.KEY_FIRST_TIME_TOGGLE
import com.androiddevs.runningapp.other.Constants.Companion.KEY_NAME
import com.androiddevs.runningapp.other.Constants.Companion.KEY_WEIGHT
import com.androiddevs.runningapp.other.Constants.Companion.SHARED_PREFERENCES_NAME
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_setup.*
import timber.log.Timber

class SetupFragment : BaseFragment(R.layout.fragment_setup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment2_to_runFragment2)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val firstTimeAppOpen =
            requireContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).getBoolean(
                KEY_FIRST_TIME_TOGGLE,
                true
            )
        if (!firstTimeAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment2, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment2_to_runFragment2,
                savedInstanceState,
                navOptions
            )
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val sharedPref =
            requireContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        val name = etName.text.toString()
        val weightText = etWeight.text.toString()
        if (name.isEmpty() || weightText.isEmpty()) {
            Snackbar.make(requireView(), "Please enter all the fields.", Snackbar.LENGTH_SHORT)
                .show()
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        return true
    }

}