package com.androiddevs.runningapp.di

import com.androiddevs.runningapp.ui.fragments.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun bindSetupFragment(): SetupFragment

    @ContributesAndroidInjector
    abstract fun bindRunFragment(): RunFragment

    @ContributesAndroidInjector
    abstract fun bindStatisticsFragment(): StatisticsFragment

    @ContributesAndroidInjector
    abstract fun bindSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun bindTrackingFragment(): TrackingFragment
}