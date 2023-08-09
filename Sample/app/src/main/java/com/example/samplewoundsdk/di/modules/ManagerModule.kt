package com.example.samplewoundsdk.di.modules

import android.content.res.Resources
import com.example.samplewoundsdk.managers.SampleDateTimeManager
import com.example.samplewoundsdk.managers.impl.SampleDateTimeManagerImpl

import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class ManagerModule {

    @Provides
    @Singleton
    fun provideSampleDateTimeManager(resources: Resources): SampleDateTimeManager =
        SampleDateTimeManagerImpl(resources)

}
