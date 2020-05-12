package com.androiddevs.runningapp.di

import com.androiddevs.runningapp.di.fragments.*
import com.androiddevs.runningapp.ui.fragments.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsBuilderModule {

    @ContributesAndroidInjector(modules = [SetupFragmentModule::class])
    abstract fun bindSetupFragment(): SetupFragment

    @ContributesAndroidInjector(modules = [RunFragmentModule::class])
    abstract fun bindRunFragment(): RunFragment

    @ContributesAndroidInjector(modules = [StatisticsFragmentModule::class])
    abstract fun bindStatisticsFragment(): StatisticsFragment

    @ContributesAndroidInjector(modules = [SettingsFragmentModule::class])
    abstract fun bindSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector(modules = [TrackingFragmentModule::class])
    abstract fun bindTrackingFragment(): TrackingFragment
}