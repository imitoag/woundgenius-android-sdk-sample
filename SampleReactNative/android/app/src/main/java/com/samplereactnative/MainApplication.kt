package com.samplereactnative

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.bridge.Callback
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods
import io.imito.woundgenius.sdk.di.WoundGeniusSDK
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost

class MainApplication : Application(), ReactApplication {

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var bodyPickerActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    fun setCameraActivityResultLauncher(
        launcher: ActivityResultLauncher<Intent>
    ) {
        cameraActivityResultLauncher = launcher
    }

    fun setBodyPickerActivityResultLauncher(
        launcher: ActivityResultLauncher<Intent>
    ) {
        bodyPickerActivityResultLauncher = launcher
    }

    fun getCameraActivityResultLauncher(): ActivityResultLauncher<Intent>? {
        return cameraActivityResultLauncher
    }

    fun getBodyPickerActivityResultLauncher(): ActivityResultLauncher<Intent>? {
        return bodyPickerActivityResultLauncher
    }

  override val reactHost: ReactHost by lazy {
    getDefaultReactHost(
      context = applicationContext,
      packageList =
        PackageList(this).packages.apply {
          // Packages that cannot be autolinked yet can be added manually here, for example:
          // add(MyReactNativePackage())
            add(MySdkPackage())
        },
    )
  }

  override fun onCreate() {
    super.onCreate()
    loadReactNative(this)


      WoundGeniusSDK.init(
          application = this,
          licenseKey = ""
      )

      WoundGeniusSDK.configure(isMeasurementLineEnabled = true, isSingleAreaEnabled = false)
  }
}
