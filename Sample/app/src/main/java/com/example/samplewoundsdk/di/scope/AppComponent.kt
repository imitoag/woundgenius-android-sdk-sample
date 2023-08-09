package com.example.samplewoundsdk.di.scope

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import com.example.samplewoundsdk.core.SampleWoundSDKApplication
import com.example.samplewoundsdk.di.modules.ActivityModule
import com.example.samplewoundsdk.di.modules.AppModule
import com.example.samplewoundsdk.di.modules.FragmentModule
import com.example.samplewoundsdk.di.modules.ViewModelModule
import javax.inject.Singleton

@Singleton
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