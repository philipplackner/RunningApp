package com.androiddevs.runningapp.other

import android.os.Parcel
import android.os.Parcelable

data class MapPoint(
    val latitude: Double,
    val longitude: Double
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeDouble(latitude)
        dest?.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MapPoint> {
        override fun createFromParcel(parcel: Parcel): MapPoint {
            return MapPoint(parcel)
        }

        override fun newArray(size: Int): Array<MapPoint?> {
            return arrayOfNulls(size)
        }
    }
}