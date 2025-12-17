package io.imito.woundgenius.sample.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.imito.woundgenius.sample.di.ViewModelFactory
import io.imito.woundgenius.sample.di.ViewModelKey
import io.imito.woundgenius.sample.ui.screen.assesmentimage.AssessmentMediaViewModel
import io.imito.woundgenius.sample.ui.screen.homescreen.HomeScreenViewModel
import io.imito.woundgenius.sample.ui.screen.main.MainViewModel
import io.imito.woundgenius.sample.ui.screen.measurementresult.holder.MeasurementResultHolderViewModel
import io.imito.woundgenius.sample.ui.screen.settings.SettingsScreenViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MeasurementResultHolderViewModel::class)
    abstract fun bindMeasurementResultHolderViewModel(viewModel: MeasurementResultHolderViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AssessmentMediaViewModel::class)
    abstract fun bindAssessmentImageViewModel(viewModel: AssessmentMediaViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeScreenViewModel::class)
    abstract fun bindHomeScreenViewModel(viewModel: HomeScreenViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsScreenViewModel::class)
    abstract fun bindSettingsScreenViewModel(viewModel: SettingsScreenViewModel): ViewModel

}
