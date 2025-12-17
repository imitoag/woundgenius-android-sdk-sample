package io.imito.woundgenius.sample.di.modules

import io.imito.woundgenius.sample.ui.screen.main.MainActivity
import io.imito.woundgenius.sample.ui.screen.measurementresult.holder.MeasurementResultHolderActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeMeasurementResultHolderActivity(): MeasurementResultHolderActivity
}
