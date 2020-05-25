package com.androiddevs.runningapp.di.home

import androidx.lifecycle.ViewModel
import com.androiddevs.runningapp.di.ViewModelKey
import com.androiddevs.runningapp.ui.HomeViewModel
import com.androiddevs.runningapp.ui.StatisticsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class StatisticsViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(StatisticsViewModel::class)
    internal abstract fun postStatisticsViewModel(viewModel: StatisticsViewModel): ViewModel
}