package com.androiddevs.runningapp.other

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DateValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = value.toLong()
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

}