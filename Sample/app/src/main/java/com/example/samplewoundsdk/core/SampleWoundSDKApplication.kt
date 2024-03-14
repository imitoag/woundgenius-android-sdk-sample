package com.example.samplewoundsdk.core

import android.graphics.Bitmap
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
import com.example.woundsdk.SdkCaptureMediaListener
import com.example.woundsdk.data.pojo.autodetectionmod.AutoDetectionMod
import com.example.woundsdk.data.pojo.cameramod.CameraMods
import com.example.woundsdk.di.WoundGeniusSDK
import com.example.woundsdk.utils.mediapicker.MediaFile
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

        val yearLicenseKey = "eyJhbGciOiIxIiwic2lnIjoiSFBXSTNwVDN5eEd6MjI2RDdiNFlhdU0ySm5vSHcyVk54M3RIb3FBQkV1cWxydFRodUxBcmM0b3FVTDNRQ0Y3SEV4TnEwWHhoVzE3TVdaSjRGbk9iUENXOEVIaHhUM2ZNUjU3NEpIQTRLVUtYV1lXdXJxd1NvaTJuYVhkSTQ0VnA0N3BLZkdHUzdvZkM1VFVYUml2MW5mY1U4clRcL3MrK2Y1R0Y4clc0TWdGZ0c3ZkFNNWNBeDdENm1NUm9VVjBIRVhDXC9GdnNDMGhrbExpbzl3K09NczR3UTBkZXd5QnA0VVpFejBRMDVrNmVoYTdMcm55VjlSakxMeDF0Y1cwOU5aWFZcL1RGUU5XM09HWEEwUEtma2hNdDFSZTBwVWNNdER4WXEra29NRitjaFFnRXgxYjJjdWFrRWFXZnY4TzZXZ1wvXC9VTzZ3c3FjYXlnb3NXZjZRQWJiUWowWndqZkVpMFhLYTE4M1hqK2dGVWRSdFd2RGxGMk9sQk9waGRKMk8wNGExRWVuVHFcL1VtaFExUU92SFByUjc3QWsrZzlZYko4cWRMWHY2bFNjRDRGWU5nWnlyUUFjR1lzUWZDYmE2dUVYNkJLQUFVUmdWRGdxRW94Smk4MzlhVnJVZ29KT2pIcER2Z1Zrd1hOUTBlMFptZXZZcE9XSHNqNUExMmJIaTlLT3RZWmhQZEYrZ1g2TG4xTk1NVkc0cHFsWmVpdVNtNmlqTWtWSHNcL2NuMEZITUp5XC9PK3pIaUdvRkQzejNRNEZVYlVFUWpZcktkYlowY0I1WU5kXC9PRDhIekRaQVRwdE1ET0lKRGk4K241UmxcLzEydGh2OXlydUxncXlRZ0xmeWFKS0xYY1wvbjFjcEFDVDZVdGV2MXBwUWtoS1ZSbElCNGxOclZsT2pIZzAwVmpHZz0iLCJlbmMiOiJleUpwYm1Oc2RXUmxaQ0k2VzNzaWFXUWlPaUpqYjIwdVpYaGhiWEJzWlM1ellXMXdiR1YzYjNWdVpITmtheUlzSW5SNWNHVWlPaUpoY0hCc2FXTmhkR2x2Ymtsa0luMHNleUowZVhCbElqb2lZWEJ3YkdsallYUnBiMjVKWkNJc0ltbGtJam9pWTI5dExtVjRZVzF3YkdVdWQyOTFibVJ6WkdzaWZTeDdJblI1Y0dVaU9pSm1aV0YwZFhKbFNXUWlMQ0pwWkNJNkltSnZaSGxRWVhKMFVHbGphMlZ5SW4wc2V5SnBaQ0k2SW5kdmRXNWtSR1YwWldOMGFXOXVJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWFXUWlPaUowYVhOemRXVlVlWEJsUkdWMFpXTjBhVzl1SWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSXNJbWxrSWpvaWNHaHZkRzlEWVhCMGRYSnBibWNpZlN4N0ltbGtJam9pZG1sa1pXOURZWEIwZFhKcGJtY2lMQ0owZVhCbElqb2labVZoZEhWeVpVbGtJbjBzZXlKcFpDSTZJbWhoYm1SNWMyTnliM0JsUTJGd2RIVnlhVzVuSWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSXNJbWxrSWpvaWNuVnNaWEpOWldGemRYSmxiV1Z1ZEVOaGNIUjFjbWx1WnlKOUxIc2lhV1FpT2lKdFlYSnJaWEpOWldGemRYSmxiV1Z1ZEVOaGNIUjFjbWx1WnlJc0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaWZTeDdJblI1Y0dVaU9pSm1aV0YwZFhKbFNXUWlMQ0pwWkNJNkltMWhiblZoYkUxbFlYTjFjbVZ0Wlc1MFNXNXdkWFFpZlN4N0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaUxDSnBaQ0k2SW0xMWJIUnBjR3hsVjI5MWJtUnpVR1Z5U1cxaFoyVWlmU3g3SW1sa0lqb2liRzlqWVd4VGRHOXlZV2RsU1cxaFoyVnpJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWFXUWlPaUpzYjJOaGJGTjBiM0poWjJWV2FXUmxiM01pTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SjBlWEJsSWpvaVptVmhkSFZ5WlVsa0lpd2lhV1FpT2lKaVlYSmpiMlJsVTJOaGJtNXBibWNpZlYwc0ltUmhkR0VpT250OUxDSnRaWFJoSWpwN0ltVjRjR2x5ZVNJNklqSXdNalF0TURndE16RWdNVE02TkRRNk1EQWlMQ0pwYzNOMVpXUWlPaUl5TURJekxUQTRMVE14SURFek9qUTBPakUwSW4xOSJ9"
        WoundGeniusSDK.init(
            application = this,
            appBundleId = BuildConfig.APPLICATION_ID,
            licenseKey = yearLicenseKey
        )
        // Configuration settings for SDK, comment to use default settings
        WoundGeniusSDK.configure(
            isAddBodyPickerOnCaptureScreenAvailable = true,
            maxNumberOfMedia = 2,
            isMultipleOutlinesEnabled = true,
            autoDetectionMod = AutoDetectionMod.WOUND
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
