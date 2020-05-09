package com.androiddevs.runningapp.di.fragments

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SetupFragmentModule {

    @Provides
    fun provideTestString() = "MOIN"
}