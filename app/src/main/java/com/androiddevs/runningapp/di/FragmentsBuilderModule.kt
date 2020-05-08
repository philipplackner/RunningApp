package com.androiddevs.runningapp.di

import com.androiddevs.runningapp.di.fragments.RunFragmentModule
import com.androiddevs.runningapp.di.fragments.SettingsFragmentModule
import com.androiddevs.runningapp.di.fragments.SetupFragmentModule
import com.androiddevs.runningapp.di.fragments.StatisticsFragmentModule
import com.androiddevs.runningapp.ui.fragments.RunFragment
import com.androiddevs.runningapp.ui.fragments.SettingsFragment
import com.androiddevs.runningapp.ui.fragments.SetupFragment
import com.androiddevs.runningapp.ui.fragments.StatisticsFragment
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
}