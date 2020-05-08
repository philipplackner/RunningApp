package com.androiddevs.runningapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.ui.HomeActivity
import kotlinx.android.synthetic.main.fragment_setup.*

class SetupFragment : BaseFragment(R.layout.fragment_setup) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvContinue.setOnClickListener {
            findNavController().navigate(R.id.action_setupFragment_to_homeActivity)
            activity?.finish()
        }
    }
}