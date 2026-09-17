package io.imito.woundgenius.sample.di.scope

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import io.imito.woundgenius.sample.core.SampleWoundSDKApplication
import io.imito.woundgenius.sample.di.modules.ActivityModule
import io.imito.woundgenius.sample.di.modules.AppModule
import io.imito.woundgenius.sample.di.modules.FragmentModule
import io.imito.woundgenius.sample.di.modules.ViewModelModule

@AppScope
@Component(
    modules = [
        AppModule::class,
        ViewModelModule::class,
        ActivityModule::class,
        FragmentModule::class,
        AndroidInjectionModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder


        fun build(): AppComponent
    }

    fun inject(application: SampleWoundSDKApplication)
}