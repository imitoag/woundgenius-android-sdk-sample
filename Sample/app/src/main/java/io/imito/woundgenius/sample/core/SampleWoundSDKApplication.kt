package io.imito.woundgenius.sample.core

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import io.imito.woundgenius.sample.AppLifecycleObserver
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.di.scope.AppComponent
import io.imito.woundgenius.sample.di.scope.DaggerAppComponent
import io.imito.woundgenius.sample.utils.FileLogTree
import io.imito.woundgenius.sdk.data.pojo.WoundGeniusOperatingMode
import io.imito.woundgenius.sdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods
import io.imito.woundgenius.sdk.di.WoundGeniusSDK
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
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

        MultiDex.install(this)
        Timber.plant(Timber.DebugTree())
        Timber.plant(FileLogTree(this).apply {
            clearOldLogs()
        })

        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build().apply { inject(this@SampleWoundSDKApplication) }

      WoundGeniusSDK.init(
            application = this,
            licenseKey = ""
        )

        WoundGeniusSDK.configure(
            defaultMode = CameraMods.MARKER_DETECT_MODE,
            primaryButtonColor = R.color.sample_app_button_color.toString(),
            maxNumberOfMedia = 100,
            isRightNavBarButtonAvailable = true,
            completionButtonTitle = null,
            isDepthInputEnabled = true,
            isDarkThemeSupported = true,
            isMinNumberOfMediaRequired = false,
            woundAutoDetectionMode = WoundAutoDetectionMode.NONE,
            woundGeniusOperatingMode = WoundGeniusOperatingMode.SDK,
            captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE),
            captureScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_SUBTITLE),

            pinsScreenTitle = getString(R.string.WOUND_GENIUS_SDK_PINS_SCREEN_TITLE),
            pinsScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_PINS_SCREEN_SUBTITLE),

            outlineScreenTitle = getString(R.string.WOUND_GENIUS_SDK_OUTLINE_SCREEN_TITLE),
            outlineScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_OUTLINE_SCREEN_SUBTITLE),

            resultScreenTitle = getString(R.string.WOUND_GENIUS_SDK_RESULTS_SCREEN_TITLE),
            resultScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_RESULTS_SCREEN_SUBTITLE)
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
