package io.imito.woundgenius.sample.core

import android.util.Log
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
            "eyJlbmMiOiJleUprWVhSaElqcDdmU3dpYVc1amJIVmtaV1FpT2x0N0ltbGtJam9pYVc4dWFXMXBkRzh1ZDI5MWJtUm5aVzVwZFhNdWMyRnRjR3hsSWl3aWRIbHdaU0k2SW1Gd2NHeHBZMkYwYVc5dVNXUWlmU3g3SW1sa0lqb2ljR2h2ZEc5RFlYQjBkWEpwYm1jaUxDSjBlWEJsSWpvaVptVmhkSFZ5WlVsa0luMHNleUpwWkNJNkluWnBaR1Z2UTJGd2RIVnlhVzVuSWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKeWRXeGxjazFsWVhOMWNtVnRaVzUwUTJGd2RIVnlhVzVuSWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKdFlYSnJaWEpOWldGemRYSmxiV1Z1ZEVOaGNIUjFjbWx1WnlJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJbWxrSWpvaWJHbHVaVTFsWVhOMWNtVnRaVzUwSWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKbWNtOXVkR0ZzUTJGdFpYSmhJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWFXUWlPaUpoY21WaFUyTmhibTVwYm1jelJDSXNJblI1Y0dVaU9pSm1aV0YwZFhKbFNXUWlmU3g3SW1sa0lqb2liWFZzZEdsd2JHVlhiM1Z1WkhOUVpYSkpiV0ZuWlNJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJbWxrSWpvaWQyOTFibVJFWlhSbFkzUnBiMjRpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW14cGRtVlhiM1Z1WkVSbGRHVmpkR2x2YmlJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJbWxrSWpvaVltOWtlVkJoY25SUWFXTnJaWElpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW14dlkyRnNVM1J2Y21GblpVbHRZV2RsY3lJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJbWxrSWpvaWJHOWpZV3hUZEc5eVlXZGxWbWxrWlc5eklpd2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSjlMSHNpYVdRaU9pSjBhWE56ZFdWVWVYQmxSR1YwWldOMGFXOXVJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWFXUWlPaUp6ZEc5dFlVUnZZM1Z0Wlc1MFlYUnBiMjRpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW1KaGNtTnZaR1ZUWTJGdWJtbHVaeUlzSW5SNWNHVWlPaUptWldGMGRYSmxTV1FpZlN4N0ltbGtJam9pWm1GamFXRnNVM1Z5WjJWeWVTSXNJblI1Y0dVaU9pSm1aV0YwZFhKbFNXUWlmU3g3SW1sa0lqb2liV0Z1ZFdGc1RXVmhjM1Z5WlcxbGJuUkpibkIxZENJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJbWxrSWpvaWFHRnVaSGx6WTI5d1pVTmhjSFIxY21sdVp5SXNJblI1Y0dVaU9pSm1aV0YwZFhKbFNXUWlmU3g3SW1sa0lqb2laR1ZpZFdkTmIyUmxJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWFXUWlPaUpoYm1Gc2VYUnBZM01pTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW5OcGJtZHNaVUZ5WldGTmIyUmxJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWFXUWlPaUpqYjI1bWFXZDFjbUZpYkdWUGNtUmxjazFoY210bGNuTlZVa3dpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW5Sb1pYSmhjSGxEUkZNaUxDSjBlWEJsSWpvaVptVmhkSFZ5WlVsa0luMWRMQ0p0WlhSaElqcDdJbWx6YzNWbFpDSTZJakl3TWpZdE1EVXRNREVnTVRZNk1URTZOVFVpZlgwPSIsInNpZyI6IkNFRzRpc2hZMlBmcFJcL09zRnZNV3BodmJuYXdFNDc4YlVTeFVlQkFKRjRGQWd3OU52dHcrT1h2cHdVVFVvVFM2eFVocDE2Wkl1SlwvcWVXZlBTaDVrMFNKVzlRK25xZDFuaEphVTVEZGFtZ0pPM3E2dzBLTG9KOG9FYkJRRHFLYWpkXC9wMjVaNmpEaE4wekdxY3lEcjZ0K3FpOWFLbUdzUWladkNpTGJCbm4zRHBRTWM4MVV2bXFoQ3Y4dXVWd1ErcmNBNXZ3UVRWbjhXQ1wvYVhUZjluOFZFSXl6aUJ1UWZTUDRBY05PUkVQYnE3c3Rna3BCMFl0ZlBqTWYycGZ2OTdiVHVHTzZFaE9aR21QRXVqWnc5ejVwc1k3RWczVTZcL2dLc2xxcmFXUmMrbDI4TjJxRk13WVdycnpKR2hjd3h4bENSOE0xbVc2NEJVUkdPNWFzcnN4VW9HZTEzRWlsM2tWNVR6ZTBkVWRPV2FnT1JCeWRoM3lLV2h4ZGtUQmY4OXdCTG90dGhNUHFxZFpueWdwRWRWYUdybHV3USt3OXdZbENKeEoycFpMRkc2VFh0MXhhdXUzajR4Q2NJYkNZdjNmZEUzTktrb2toM1haK1l5QU5BOHpLZ2ttTEhhYTRFZkZZZ1Z6VDJGWUhGYjJiOXprT01NNjVYajlTSVBQcHBMWnZQbVdcL3FKNklUSXFNcG5lN2lMdms2ZE1nZ1VLK1wvSWhcL0dVbG5iK2s4aFRCaDJFdk13clJvaG1MSW1nZEMyQ3pyM3Y1VnMyWjdDV0x0QVU1VWtOSk15akVHRVYwV2tMb0Y0dHlMd2VDczk0Ris3anN3cG40SGFDNkR4eDB2Mm9jalVjcjZQNzBXZHR1TlNWN1laMVByNitQb0dXZXNTalJib3g4Vmhjc1d6T0k9IiwiYWxnIjoiMSJ9"
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
            Log.e("woundGeniusError",e.stackTraceToString())
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
