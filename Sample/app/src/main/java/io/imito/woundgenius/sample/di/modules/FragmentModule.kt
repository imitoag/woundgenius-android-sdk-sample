package io.imito.woundgenius.sample.di.modules

import io.imito.woundgenius.sample.ui.screen.assesmentimage.AssessmentMediaFragment
import io.imito.woundgenius.sample.ui.screen.homescreen.HomeScreenFragment
import io.imito.woundgenius.sample.ui.screen.settings.SettingsScreenFragment
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
