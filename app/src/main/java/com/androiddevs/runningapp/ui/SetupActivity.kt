package com.androiddevs.runningapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.androiddevs.runningapp.R
import dagger.android.support.DaggerAppCompatActivity

class SetupActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)


    }
}
