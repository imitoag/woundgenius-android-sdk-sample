package com.example.samplewoundsdk.core

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.samplewoundsdk.AppLifecycleObserver
import com.example.samplewoundsdk.R
import com.orhanobut.hawk.Hawk
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import com.example.samplewoundsdk.di.scope.AppComponent
import com.example.samplewoundsdk.di.scope.DaggerAppComponent
import com.example.samplewoundsdk.BuildConfig
import com.example.woundsdk.data.pojo.cameramod.CameraMods
import com.example.woundsdk.di.WoundGeniusSDK
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

class SampleWoundSDKApplication : MultiDexApplication(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    lateinit var appComponent: AppComponent

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()

        Hawk.init(this).build()
        MultiDex.install(this)
        Timber.plant(Timber.DebugTree())

        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build().apply { inject(this@SampleWoundSDKApplication) }

      WoundGeniusSDK.init(
            application = this,
            appBundleId = BuildConfig.APPLICATION_ID
        )
        // Configuration settings for SDK, comment to use default settings
        WoundGeniusSDK.configure(
            isAddBodyPickerOnCaptureScreenAvailable = true,
            maxNumberOfMedia = 2,
            isMultipleOutlinesEnabled = true
        )

        RxJavaPlugins.setErrorHandler { e: Throwable ->
            var error = e
            if (error is UndeliverableException) {
                error.cause?.let { error = it }
            }
            if (error is IOException || error is SocketException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (error is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (error is NullPointerException || error is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler
                    ?.uncaughtException(Thread.currentThread(), error)
                return@setErrorHandler
            }
            if (error is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler
                    ?.uncaughtException(Thread.currentThread(), error)
                return@setErrorHandler
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
