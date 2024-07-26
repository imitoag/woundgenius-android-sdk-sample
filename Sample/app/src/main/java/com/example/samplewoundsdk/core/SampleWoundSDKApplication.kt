package com.example.samplewoundsdk.core

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.example.samplewoundsdk.AppLifecycleObserver
import com.orhanobut.hawk.Hawk
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import com.example.samplewoundsdk.di.scope.AppComponent
import com.example.samplewoundsdk.di.scope.DaggerAppComponent
import com.example.samplewoundsdk.BuildConfig
import com.example.samplewoundsdk.R
import com.example.woundsdk.data.pojo.WoundGeniusOperatingMode
import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.di.WoundGeniusSDK
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

        var yearLicenseKey = "eyJlbmMiOiJleUpwYm1Oc2RXUmxaQ0k2VzNzaWFXUWlPaUpqYjIwdVpYaGhiWEJzWlM1ellXMXdiR1YzYjNWdVpITmtheUlzSW5SNWNHVWlPaUpoY0hCc2FXTmhkR2x2Ymtsa0luMHNleUpwWkNJNkluQm9iM1J2UTJGd2RIVnlhVzVuSWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKMmFXUmxiME5oY0hSMWNtbHVaeUlzSW5SNWNHVWlPaUptWldGMGRYSmxTV1FpZlN4N0luUjVjR1VpT2lKbVpXRjBkWEpsU1dRaUxDSnBaQ0k2SW5KMWJHVnlUV1ZoYzNWeVpXMWxiblJEWVhCMGRYSnBibWNpZlN4N0ltbGtJam9pYldGeWEyVnlUV1ZoYzNWeVpXMWxiblJEWVhCMGRYSnBibWNpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW1aeWIyNTBZV3hEWVcxbGNtRWlMQ0owZVhCbElqb2labVZoZEhWeVpVbGtJbjBzZXlKMGVYQmxJam9pWm1WaGRIVnlaVWxrSWl3aWFXUWlPaUp0ZFd4MGFYQnNaVmR2ZFc1a2MxQmxja2x0WVdkbEluMHNleUpwWkNJNkluZHZkVzVrUkdWMFpXTjBhVzl1SWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKc2FYWmxWMjkxYm1SRVpYUmxZM1JwYjI0aUxDSjBlWEJsSWpvaVptVmhkSFZ5WlVsa0luMHNleUpwWkNJNkltSnZaSGxRWVhKMFVHbGphMlZ5SWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKc2IyTmhiRk4wYjNKaFoyVkpiV0ZuWlhNaUxDSjBlWEJsSWpvaVptVmhkSFZ5WlVsa0luMHNleUowZVhCbElqb2labVZoZEhWeVpVbGtJaXdpYVdRaU9pSnNiMk5oYkZOMGIzSmhaMlZXYVdSbGIzTWlmVjBzSW1SaGRHRWlPbnQ5TENKdFpYUmhJanA3SW1semMzVmxaQ0k2SWpJd01qUXRNRGN0TWpZZ01EZzZORFU2TURraUxDSmxlSEJwY25raU9pSXlNREkxTFRBM0xUSTJJREE0T2pRMU9qQXdJbjE5Iiwic2lnIjoiUVwvN3VuZlNKTzU5K3BQUXJjeG9DUTB4dWdoVFNMWkxmaU1iNmRIYysxb0ptZzJaSXdCd255eE16dHh0WkFKZlBBeFJqQ0xqb2tqbWF0Z2I5Ym5FRTZGVU52SmxUUkFIekJBazVmeUc5SzNlUU5UeFZacjdZcnh5NFJxY2JyOUZYNlVNMktpOHg1RVVrR0pFQTNqN2h6T1o4eG5ZTDJGb2xOd2VGNVZ2Y0F5eHJEY1RlamdoZlJJMWhVUGpGeUFBU2hwZ2w4eitSUW1MditxUFlQbk9vWlN1ZHEwTVhrRHlJZlRHenFDV1l5S0xzVmVKZWorT05ESHVLVzRwS1E4K1B1M2tORG9sc1crd2k2UTVcLzZIT05McUN0TUljc3MwanRBajRnc2xtT1AxSFV4XC81WFwvbDNUaXgrSUZSQ0R4NlV3QWNGSjJoNVl4ZUh0bXBicGRqZ24wXC9STTl6TjVUSGNhcUw3emNwc3RKdjRnVEpnUkFDUGdEV0FUMDh1c2g4M3N2MHArTWFkV2VPenUyVDQ4b1NaSXpMTnBMWHR4ZHIyWEE0TGpVVnZwVXBaemJHeFNCTkx5SXlxOXJGRnlSN3E1aTFNNjA1TkU5UUdMSEdJemRDMmFWN1wvTnVpUWdTWkRMWXNSM3lFWHhIT2VyeW00QU9oUGQ3d2NERGNITjhialZVOUpzXC8zZHZRMHd2Z0w5bFhQSFF6a05oR2hGb2dWZ0l5Y1dUVVNENmdkVzRPbHdJOWVQRGMzcEpWM2dJWmhtSFNXUG9OOENHTzRVXC9lam1TTWV0XC9rNUdaZEVWRlN0RmdZaVVJYVdBaTVvdDEramZXK2JOY0NCVk9za3JoeXdWSXI3cjNlaDJtaHFzaFVqUEFmaFF5RlM3N0hjVmNFd0YyWVBGOEtMT0FrM2c9IiwiYWxnIjoiMSJ9"
        WoundGeniusSDK.init(
            application = this,
            appBundleId = BuildConfig.APPLICATION_ID,
            licenseKey = yearLicenseKey
        )

        WoundGeniusSDK.configure(
            maxNumberOfMedia = 10,
            isStomaFlow = false,
            isAddFromLocalStorageAvailable = true,
            woundAutoDetectionMode = WoundAutoDetectionMode.NONE,
            isLiveDetectionEnabled = false,
            isMultipleOutlinesEnabled = true,
            isMinNumberOfMediaRequired = true
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
