package com.example.samplewoundsdk.core

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.samplewoundsdk.AppLifecycleObserver
import com.example.samplewoundsdk.BuildConfig
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.di.scope.AppComponent
import com.example.samplewoundsdk.di.scope.DaggerAppComponent
import com.example.samplewoundsdk.utils.FileLogTree
import com.example.woundsdk.SdkCaptureMediaListener
import com.example.woundsdk.data.pojo.WoundGeniusOperatingMode
import com.example.woundsdk.data.pojo.WoundGeniusSdkInnerIntent
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.data.pojo.cameramod.CameraMods
import com.example.woundsdk.data.pojo.entity.MediaModel
import com.example.woundsdk.data.pojo.measurement.ImageResolution
import com.example.woundsdk.di.WoundGeniusSDK
import com.orhanobut.hawk.Hawk
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import java.io.File
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
        Timber.plant(FileLogTree(this).apply {
            clearOldLogs()
        })

        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build().apply { inject(this@SampleWoundSDKApplication) }

        var  yearLicenseKey = "eyJlbmMiOiJleUprWVhSaElqcDdmU3dpYldWMFlTSTZleUpsZUhCcGNua2lPaUl5TURJMUxURXlMVEF6SURFeE9qUXhPakF3SWl3aWFYTnpkV1ZrSWpvaU1qQXlOQzB4TWkwd01pQXhNVG8wTVRvMU15SjlMQ0pwYm1Oc2RXUmxaQ0k2VzNzaWFXUWlPaUpqYjIwdVpYaGhiWEJzWlM1ellXMXdiR1YzYjNWdVpITmtheUlzSW5SNWNHVWlPaUpoY0hCc2FXTmhkR2x2Ymtsa0luMHNleUowZVhCbElqb2labVZoZEhWeVpVbGtJaXdpYVdRaU9pSndhRzkwYjBOaGNIUjFjbWx1WnlKOUxIc2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSXNJbWxrSWpvaWRtbGtaVzlEWVhCMGRYSnBibWNpZlN4N0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaUxDSnBaQ0k2SW5KMWJHVnlUV1ZoYzNWeVpXMWxiblJEWVhCMGRYSnBibWNpZlN4N0ltbGtJam9pYldGeWEyVnlUV1ZoYzNWeVpXMWxiblJEWVhCMGRYSnBibWNpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW1aeWIyNTBZV3hEWVcxbGNtRWlMQ0owZVhCbElqb2labVZoZEhWeVpVbGtJbjBzZXlKcFpDSTZJbTExYkhScGNHeGxWMjkxYm1SelVHVnlTVzFoWjJVaUxDSjBlWEJsSWpvaVptVmhkSFZ5WlVsa0luMHNleUpwWkNJNkluZHZkVzVrUkdWMFpXTjBhVzl1SWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSXNJbWxrSWpvaWJHbDJaVmR2ZFc1a1JHVjBaV04wYVc5dUluMHNleUpwWkNJNkltSnZaSGxRWVhKMFVHbGphMlZ5SWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKc2IyTmhiRk4wYjNKaFoyVkpiV0ZuWlhNaUxDSjBlWEJsSWpvaVptVmhkSFZ5WlVsa0luMHNleUowZVhCbElqb2labVZoZEhWeVpVbGtJaXdpYVdRaU9pSnNiMk5oYkZOMGIzSmhaMlZXYVdSbGIzTWlmU3g3SW5SNWNHVWlPaUptWldGMGRYSmxTV1FpTENKcFpDSTZJblJwYzNOMVpWUjVjR1ZFWlhSbFkzUnBiMjRpZlN4N0ltbGtJam9pYzNSdmJXRkViMk4xYldWdWRHRjBhVzl1SWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSXNJbWxrSWpvaVltRnlZMjlrWlZOallXNXVhVzVuSW4wc2V5SjBlWEJsSWpvaVptVmhkSFZ5WlVsa0lpd2lhV1FpT2lKdFlXNTFZV3hOWldGemRYSmxiV1Z1ZEVsdWNIVjBJbjBzZXlKMGVYQmxJam9pWm1WaGRIVnlaVWxrSWl3aWFXUWlPaUpvWVc1a2VYTmpiM0JsUTJGd2RIVnlhVzVuSW4wc2V5SjBlWEJsSWpvaVptVmhkSFZ5WlVsa0lpd2lhV1FpT2lKa1pXSjFaMDF2WkdVaWZWMTkiLCJhbGciOiIxIiwic2lnIjoiWDhYK3pjdjRncHRQMzdcL2xyM2VoTndKWXdZdXVKcmVyclZXSHlmVDl3YVVLWXowRDh0VWtscm85blg0TzlMSTFcLzNNcHpHTlMyWDEyMHRObU03Z0d3WE43ZG9EeGhGMjhudVVJVHo4VXgxVDliaG1Ud3VPNWxcLzJ3UTQxakFjRXJIMzNjTnQ0cVBpa2tKV1VKaFFHV1hWU2hXYlk2MmQ3UmxFYTNHUWFnQlwvMitGNEppMmFFK1BibW5DSDl3R3FQWUlnV1RZVFpPdUVsYzVSWTdVWmdsZk5ORnZ0RkVPd3RWMHllZUpMNzdmclMrMkFqa0U5SHRwb3dNeGxMU25CMFVOcTRmMElhbVpUcnh1dkg0TVpuVW50Y1FkT0VnNDVhQzZuYmt2YUxxeWNsd1JEQytjNFcwK3pzb2hYQ0RGUmNKV0p6UE1XdjRSb0FyaGloZE9KdUZ0Y2dYaCtCWUdWU2l4bkhsSjRjU2RXOTE4UkgzQ2hQcFZwcFNUcGVuZXFzb2ZZR0hzOVFOb2V0b3VUM1pLREJ6c2w0cERmMzFRU0Y4Z2ZyWHorTU13ZkU3MG82NXZmcnhUXC9PWWJONkFlcUViemdqVnZuOFJzOUQxeWpEdW1EM0xRN1grRWc3dVwvMlMzZkMwNTczTWUzUE9la1o1eTQraGZSTldranR4NjVXSmpvXC9abjVzbnNMRmozRWlFMjZHa1JWNm5vdjBJUGF4N2k1TXk3eFRBcXE1aG5kUnBCeHZ4QzVHb3VOdENHZDhmNW9mMWhReEU5YVdcL1I5d0krMXJYcUtIbFE0R2ljcllTYnQ4cnFGczluNndcLzNSTlNvczBLdkhxbHQ0Tk5Sc2NDd09GYTZFemgyWlNBUHpZb3oxSmU3MTRJY041WTd1MjF3QkVpY2Jnckg4c2M9In0="
        WoundGeniusSDK.init(
            application = this,
            appBundleId = BuildConfig.APPLICATION_ID,
            licenseKey = yearLicenseKey
        )

        WoundGeniusSDK.configure(
            defaultMode = CameraMods.MARKER_DETECT_MODE,
            primaryButtonColor = R.color.sample_app_button_color.toString(),
            maxNumberOfMedia = 100,
            isRightNavBarButtonAvailable = true,
            completionButtonTitle = null,
            isDepthInputEnabled = false,
            isDarkThemeSupported = true,
            isMinNumberOfMediaRequired = false,
            woundAutoDetectionMode = WoundAutoDetectionMode.NONE,
            woundGeniusOperatingMode = WoundGeniusOperatingMode.SDK
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
