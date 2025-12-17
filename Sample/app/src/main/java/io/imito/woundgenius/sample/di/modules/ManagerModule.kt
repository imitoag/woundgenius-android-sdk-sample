package io.imito.woundgenius.sample.di.modules

import android.content.res.Resources
import io.imito.woundgenius.sample.managers.SampleDateTimeManager
import io.imito.woundgenius.sample.managers.impl.SampleDateTimeManagerImpl

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
