package com.androiddevs.runningapp.other

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DateValueFormatter : ValueFormatter() {

    val sdf = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

    override fun getFormattedValue(value: Float, axis: AxisBase): String {

        return super.getFormattedValue(value)
    }
}