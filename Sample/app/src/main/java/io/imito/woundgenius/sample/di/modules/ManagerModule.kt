package io.imito.woundgenius.sample.di.modules

import android.content.res.Resources
import io.imito.woundgenius.sample.managers.SampleDateTimeManager
import io.imito.woundgenius.sample.managers.impl.SampleDateTimeManagerImpl

import dagger.Module
import dagger.Provides
import io.imito.woundgenius.sample.di.scope.AppScope
import javax.inject.Singleton


@Module
class ManagerModule {

    @Provides
    @AppScope
    fun provideSampleDateTimeManager(resources: Resources): SampleDateTimeManager =
        SampleDateTimeManagerImpl(resources)

}
