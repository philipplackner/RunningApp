package com.androiddevs.runningapp.ui

import android.os.Bundle
import com.androiddevs.runningapp.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_setup.*

class SetupActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        setSupportActionBar(toolbar)
    }
}
