package com.samplereactnative

import android.content.Intent
import io.imito.woundgenius.sdk.ui.screen.measurecamera.MeasureCameraActivity
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods
import io.imito.woundgenius.sdk.ui.screen.bodypicker.BodyPickerActivity
import io.imito.woundgenius.sdk.ui.screen.support.MeasureSupportActivity
import java.io.File


class ExampleSdkModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "ExampleSdkModule"


    @ReactMethod
    fun launchSdkFromNativeActivity() {
        val currentActivity = reactContext.currentActivity
        val intent = Intent(currentActivity, SDKLauchActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }

    @ReactMethod
    fun launchSdkCameraWithResult(callback: Callback) {
        val currentActivity = reactContext.currentActivity
        val activityResultLauncher =
            (reactContext.applicationContext as MainApplication).getCameraActivityResultLauncher()


        if (currentActivity != null) {
            if (currentActivity is MainActivity) {
                currentActivity.setSDKCallback(callback)
            }
            val previewDir = File(reactContext.cacheDir, "sdkImages")
            if (!previewDir.exists()) {
                previewDir.mkdir()
            }
            if (activityResultLauncher != null) {
                MeasureCameraActivity.openWithResult(
                    activityResultLauncher,
                    currentActivity,
                    previewDir.absolutePath //change the directory to whatever you want
                )
            } else {
                callback.invoke("Error: ActivityResultLauncher is null. Is MainActivity running?")
            }
        } else {
            callback.invoke("Error: Activity unavailable")
        }
    }

    @ReactMethod
    fun launchSdkBodyPickerWithResult(callback: Callback) {
        val currentActivity = reactContext.currentActivity
        val activityResultLauncher =
            (reactContext.applicationContext as MainApplication).getBodyPickerActivityResultLauncher()


        if (currentActivity != null) {
            if (currentActivity is MainActivity) {
                currentActivity.setSDKCallback(callback)
            }
            if (activityResultLauncher != null) {
                BodyPickerActivity.openWithResult(
                    activityResultLauncher,
                    currentActivity,
                    null,
                    null,
                    null
                )
            } else {
                callback.invoke("Error: ActivityResultLauncher is null. Is MainActivity running?")
            }
        } else {
            callback.invoke("Error: Activity unavailable")
        }
    }

    @ReactMethod
    fun launchSdkHelpScreen(callback: Callback) {
        val currentActivity = reactContext.currentActivity

        if (currentActivity is MainActivity) {
            MeasureSupportActivity.open(
                currentActivity,
                CameraMods.MARKER_DETECT_MODE
            )
        }
    }
}