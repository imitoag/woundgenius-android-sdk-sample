package com.awesomeproject

import android.content.Intent
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.io.File


class ExampleSdkModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {


    private val activityResultLauncher =
        (reactContext.applicationContext as MainApplication).getActivityResultLauncher()

    override fun getName(): String {
        return "ExampleSdkModule"
    }


    @ReactMethod
    fun launchSdkFromNativeActivity() {
        val currentActivity = currentActivity
        val intent = Intent(currentActivity, SDKLauchActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }

    @ReactMethod
    fun launchSdkWithResult(callback: Callback) {
        val currentActivity = currentActivity

        if (currentActivity != null) {
            if (currentActivity is MainActivity) {
                currentActivity.setSDKCallback(callback)
            }
            val previewDir = File(reactContext.cacheDir,"sdkImages")
            if (!previewDir.exists()) {
                previewDir.mkdir()
            }
            if (activityResultLauncher != null) {
                MeasureCameraActivity.openWithResult(
                    activityResultLauncher,
                    currentActivity,
                    previewDir.absolutePath //change the directory to whatever you want
                )
            }
        } else {
            callback.invoke("Error: Activity unavailable")
        }
    }

}
