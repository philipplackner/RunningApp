package com.androiddevs.runningapp.di.fragments

import com.androiddevs.runningapp.adapters.RunAdapter
import dagger.Module
import dagger.Provides

@Module
class RunFragmentModule {

    @Provides
    fun provideRunAdapter() = RunAdapter()
}