package com.androiddevs.runningapp.di

import com.androiddevs.runningapp.di.home.HomeModule
import com.androiddevs.runningapp.di.home.HomeViewModelModule
import com.androiddevs.runningapp.ui.HomeActivity
import com.androiddevs.runningapp.ui.SetupActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSetupActivity(): SetupActivity

    @ContributesAndroidInjector(modules = [HomeViewModelModule::class, HomeModule::class])
    abstract fun contributeHomeActivity(): HomeActivity
}