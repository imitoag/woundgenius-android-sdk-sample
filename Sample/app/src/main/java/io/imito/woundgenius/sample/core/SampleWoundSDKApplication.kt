package io.imito.woundgenius.sample.core

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.imito.wizard.api.di.WizardSDK
import io.imito.woundgenius.sample.AppLifecycleObserver
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.di.scope.AppComponent
import io.imito.woundgenius.sample.utils.FileLogTree
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.api.models.configuration.WoundGeniusConfiguration
import io.imito.woundgenius.sdk.api.models.presenter.WGPresenter
import io.imito.woundgenius.sdk.internal.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.internal.data.pojo.mode.OperatingMode
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject
import io.imito.woundgenius.sample.di.scope.DaggerAppComponent as AppDaggerComponent
//import io.imito.woundgenius.sdk.internal.managers.wizard.DaggerWizardAppComponent as WizardDaggerComponent

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


        appComponent = AppDaggerComponent.builder()
            .application(this)
            .build()

       
        appComponent.inject(this)


        var yearLicenseKey =
            "eyJzaWciOiJMQ3FXSUJranpQUnUxb0M3aVdwXC9MOU5MRDR1SmdVNzh2bzNvMUpNdFduQ01ZSGZmUkh3MXp5ZHdFYnRTbUxRNVpPRDZcL2NoYVE2ZkF6NWRXZTE3ck1DYVRCdng4V0tUVFpLYW1CWDFDSmVFMnpnSXBPRzI0Z3gwZFRTd2ZoNGVwREx6ekliNXZMbkxqUE1vNUR2ZVlVcHAwcldPRE15Q1RvQzhPOEl5ajRhSGQ4eTI1RTR6dWpKUlpZUmxnVjBuMnZDSmlXTEIyOFZpUHB3WW9BcGc2NVV5OHMyb056V01qRkxYamxqWHFTZllOZUR6NVA5RFwvZ21jYU5ucE50bnZLT1FDaUMxWTVNd09BdFwvYWhjOGlpUFJ6cEM2dlRWNDkyTkxIcWJISGt1M0VCeUlDZElnVWsrcmJOV3FxU2NwSzhYV0t3dDFENVRnZ2VVVTN6bnVIMElGdzgxUDNhM1wvb0k0OGFaREtKaythV1dNUkNJeWtoTVhZTGNuVkN1ZWdRa3EwUDhDdWpYTUVqQVExejV4U2lkOXNEUzdMNG54dWpUOUJ3U0lJYkUyTldCSk1FeWZuQXozc2FRWWZ3TmZLXC9VNE1xOXVPVVVZKzVcL0g3bDFFZnhDZFZcLytJd0c4d3RpNHZHcmxHbkxCRllyQkM0WGMzXC9sNVJHSWE4bExBeWxqZHJLNmRVSmhaR29wcEFYTWNoSmU3a2JSRVRMVWZkeU1hRUY0ajBhQmJDNE9oNmhqb21Fc1pCR0pjK1pHWUFyZ29ObFdGaUlXZEVSMXRzbUlmaDhrNW1QMHo2WFV3SzFDN3J4UzZHOHFvVnlIXC9hcGFibGl5a2FDMlVVUmJKSXNhN2NxcjFXQnVJWmNiMnNjWVFsUEpRSzB0bDEzbjJRNkdTWGRFUWpYcG1TMlU9IiwiZW5jIjoiZXlKdFpYUmhJanA3SW1semMzVmxaQ0k2SWpJd01qWXRNRFV0TURFZ01UWTZNVEU2TlRVaWZTd2laR0YwWVNJNmUzMHNJbWx1WTJ4MVpHVmtJanBiZXlKcFpDSTZJbWx2TG1sdGFYUnZMbWx0YVhSdlRXVmhjM1Z5WlNJc0luUjVjR1VpT2lKaGNIQnNhV05oZEdsdmJrbGtJbjBzZXlKcFpDSTZJbkJvYjNSdlEyRndkSFZ5YVc1bklpd2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSjlMSHNpYVdRaU9pSnlkV3hsY2sxbFlYTjFjbVZ0Wlc1MFEyRndkSFZ5YVc1bklpd2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSjlMSHNpYVdRaU9pSnRZWEpyWlhKTlpXRnpkWEpsYldWdWRFTmhjSFIxY21sdVp5SXNJblI1Y0dVaU9pSm1aV0YwZFhKbFNXUWlmU3g3SW1sa0lqb2labkp2Ym5SaGJFTmhiV1Z5WVNJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJbWxrSWpvaWJYVnNkR2x3YkdWWGIzVnVaSE5RWlhKSmJXRm5aU0lzSW5SNWNHVWlPaUptWldGMGRYSmxTV1FpZlN4N0ltbGtJam9pZDI5MWJtUkVaWFJsWTNScGIyNGlMQ0owZVhCbElqb2labVZoZEhWeVpVbGtJbjBzZXlKcFpDSTZJbXhwZG1WWGIzVnVaRVJsZEdWamRHbHZiaUlzSW5SNWNHVWlPaUptWldGMGRYSmxTV1FpZlN4N0ltbGtJam9pWW05a2VWQmhjblJRYVdOclpYSWlMQ0owZVhCbElqb2labVZoZEhWeVpVbGtJbjBzZXlKcFpDSTZJbXh2WTJGc1UzUnZjbUZuWlVsdFlXZGxjeUlzSW5SNWNHVWlPaUptWldGMGRYSmxTV1FpZlN4N0ltbGtJam9pZEdsemMzVmxWSGx3WlVSbGRHVmpkR2x2YmlJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJbWxrSWpvaVlXNWhiSGwwYVdOeklpd2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSjlMSHNpYVdRaU9pSjBhR1Z5WVhCNVEwUlRJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5WFgwPSIsImFsZyI6IjEifQ"
        WoundGeniusSDK.init(
            application = this,
            licenseKey = yearLicenseKey
        )

        val configuration = WoundGeniusConfiguration(
            defaultMode = ImitoCameraMode.MARKER_DETECT_MODE,
            primaryButtonColor = R.color.sample_app_button_color.toString(),
            maxNumberOfMedia = 100,
            isDepthOrHeightInputEnabled = true,
            isDarkThemeSupported = true,
            minNumberOfMedia = 0,
            autoDetectionMode = WoundAutoDetectionMode.NONE,
            operatingMode = OperatingMode.SDK,
            captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE),
            captureScreenSubtitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_SUBTITLE),

            pinsScreenTitle = getString(R.string.WOUND_GENIUS_SDK_PINS_SCREEN_TITLE),
            pinsScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_PINS_SCREEN_SUBTITLE),

            outlineScreenTitle = getString(R.string.WOUND_GENIUS_SDK_OUTLINE_SCREEN_TITLE),
            outlineScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_OUTLINE_SCREEN_SUBTITLE),

            resultScreenTitle = getString(R.string.WOUND_GENIUS_SDK_RESULTS_SCREEN_TITLE),
            resultScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_RESULTS_SCREEN_SUBTITLE),
        )

        val presenter = WGPresenter(
            configuration = configuration
        )

        WoundGeniusSDK.configure(
            presenter = presenter
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
