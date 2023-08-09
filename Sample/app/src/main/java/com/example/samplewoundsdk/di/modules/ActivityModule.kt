package com.example.samplewoundsdk.di.modules

import com.example.samplewoundsdk.ui.screen.main.MainActivity
import com.example.samplewoundsdk.ui.screen.measurementfullscreen.MeasurementFullScreenActivity
import com.example.samplewoundsdk.ui.screen.measurementresult.holder.MeasurementResultHolderActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeMeasurementResultHolderActivity(): MeasurementResultHolderActivity

    @ContributesAndroidInjector
    internal abstract fun contributeMeasurementFullScreenActivity(): MeasurementFullScreenActivity
}
