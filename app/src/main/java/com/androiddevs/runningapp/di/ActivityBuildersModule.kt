package com.androiddevs.runningapp.di

import com.androiddevs.runningapp.di.home.HomeModule
import com.androiddevs.runningapp.di.home.HomeViewModelModule
import com.androiddevs.runningapp.di.tracking.TrackingViewModelModule
import com.androiddevs.runningapp.ui.HomeActivity
import com.androiddevs.runningapp.ui.SetupActivity
import com.androiddevs.runningapp.ui.TrackingActivity
import com.androiddevs.runningapp.ui.TrackingViewModel
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    // TODO: Add ViewModel
    @ContributesAndroidInjector
    abstract fun contributeSetupActivity(): SetupActivity

    @ContributesAndroidInjector(modules = [TrackingViewModelModule::class, HomeViewModelModule::class, HomeModule::class])
    abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector(modules = [TrackingViewModelModule::class])
    abstract fun contributeTrackingActivity(): TrackingActivity
}