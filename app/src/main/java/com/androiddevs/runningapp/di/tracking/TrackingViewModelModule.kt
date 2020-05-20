package com.androiddevs.runningapp.di.tracking

import androidx.lifecycle.ViewModel
import com.androiddevs.runningapp.di.ViewModelKey
import com.androiddevs.runningapp.ui.HomeViewModel
import com.androiddevs.runningapp.ui.TrackingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class TrackingViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(TrackingViewModel::class)
    internal abstract fun postTrackingViewModel(viewModel: TrackingViewModel): ViewModel
}