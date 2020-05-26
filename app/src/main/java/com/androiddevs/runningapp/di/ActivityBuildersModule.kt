package com.androiddevs.runningapp.di

import com.androiddevs.runningapp.di.viewmodel.HomeViewModelModule
import com.androiddevs.runningapp.di.viewmodel.StatisticsViewModelModule
import com.androiddevs.runningapp.ui.HomeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    // TODO: Add ViewModel
    @ContributesAndroidInjector
    abstract fun contributeSetupActivity(): SetupActivity

    @ContributesAndroidInjector(modules = [TrackingViewModelModule::class, HomeViewModelModule::class, HomeModule::class])
    abstract fun contributeHomeActivity(): HomeActivity

}