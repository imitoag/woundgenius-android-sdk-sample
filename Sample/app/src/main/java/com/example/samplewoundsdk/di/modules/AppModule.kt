package com.example.samplewoundsdk.di.modules

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.example.samplewoundsdk.utils.gson.SerializableAsNullConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        StorageModule::class,
        RepositoryModule::class,
        ManagerModule::class,
    ]
)
class AppModule {

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapterFactory(SerializableAsNullConverter())
        .create()

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @Singleton
    @Provides
    fun provideResources(context: Context): Resources = context.resources
}
