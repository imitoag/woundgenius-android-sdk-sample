package com.example.samplewoundsdk.di.modules

import com.example.samplewoundsdk.ui.screen.assesmentimage.AssessmentMediaFragment
import com.example.samplewoundsdk.ui.screen.homescreen.HomeScreenFragment
import com.example.samplewoundsdk.ui.screen.settings.SettingsScreenFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeScreenFragment(): HomeScreenFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsScreenFragment(): SettingsScreenFragment

    @ContributesAndroidInjector
    abstract fun contributeAssessmentImageFragment(): AssessmentMediaFragment
}
