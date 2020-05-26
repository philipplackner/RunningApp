package com.androiddevs.runningapp.di.viewmodel

import androidx.lifecycle.ViewModel
import com.androiddevs.runningapp.di.ViewModelKey
import com.androiddevs.runningapp.ui.HomeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class HomeViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    internal abstract fun postHomeViewModel(viewModel: HomeViewModel): ViewModel
}