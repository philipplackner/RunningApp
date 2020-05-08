package com.androiddevs.runningapp.di

import android.app.Application
import androidx.room.Room
import com.androiddevs.runningapp.db.RunDao
import com.androiddevs.runningapp.db.RunningDatabase
import com.androiddevs.runningapp.db.RunningDatabase.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideAppDb(app: Application): RunningDatabase {
        return Room.databaseBuilder(app, RunningDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase): RunDao {
        return db.getRunDao()
    }
}