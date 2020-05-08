package com.androiddevs.runningapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "running_table")
data class Run(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var img: Bitmap? = null,
    var date: Date? = null,
    var avgSpeed: Double = 0.0,
    var distance: Float = 0.0f,
    var timeInMin: Int = 0,
    var caloriesBurned: Int = 0
)