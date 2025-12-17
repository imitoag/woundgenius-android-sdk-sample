package com.samplereactnative

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import io.imito.woundgenius.sdk.data.pojo.assessment.entity.AssessmentEntity
import io.imito.woundgenius.sdk.data.pojo.bodypart.WoundGeniusBodyPart
import io.imito.woundgenius.sdk.ui.screen.bodypicker.BodyPartContract
import io.imito.woundgenius.sdk.ui.screen.measurecamera.MeasureCameraContract
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.bridge.Callback
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    override fun getMainComponentName(): String = "SampleReactNative"

    /**
     * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
     * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
     */
    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    lateinit var bodyPickerActivityResultLauncher: ActivityResultLauncher<Intent>
    private var sdkCallback: Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraActivityResultLauncher = registerForActivityResult(
            MeasureCameraContract()
        ) { assessment: AssessmentEntity? ->
            //your code to handle SDK result
            if (assessment != null) {
                sdkCallback?.invoke(assessment.toString())
            } else {
                sdkCallback?.invoke("SDK Camera no results")
            }
            sdkCallback = null
        }

        (application as MainApplication).setCameraActivityResultLauncher(cameraActivityResultLauncher)

        bodyPickerActivityResultLauncher = registerForActivityResult(
            BodyPartContract()
        ) { bodyParts: List<WoundGeniusBodyPart>? ->
            //your code to handle SDK result
            if (bodyParts != null) {
                sdkCallback?.invoke(bodyParts.toString())
            } else {
                sdkCallback?.invoke("SDK Body Picker no results")
            }
            sdkCallback = null
        }

        (application as MainApplication).setBodyPickerActivityResultLauncher(bodyPickerActivityResultLauncher)
    }

    fun setSDKCallback(callback: Callback) {
        this.sdkCallback = callback
    }
}
