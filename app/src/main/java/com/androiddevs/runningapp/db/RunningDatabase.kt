package com.androiddevs.runningapp.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Run::class],
    version = 1
)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDao

    companion object {
        const val DATABASE_NAME = "running_db"
    }
}