package com.androiddevs.runningapp.di

import com.androiddevs.runningapp.di.viewmodel.HomeViewModelModule
import com.androiddevs.runningapp.di.viewmodel.StatisticsViewModelModule
import com.androiddevs.runningapp.ui.HomeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(modules = [HomeViewModelModule::class, StatisticsViewModelModule::class])
    abstract fun contributeHomeActivity(): HomeActivity

}