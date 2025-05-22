package com.awesomeproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraContract
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
    override fun getMainComponentName(): String = "AwesomeProject"

    /**
     * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
     * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
     */
    override fun createReactActivityDelegate(): ReactActivityDelegate =
        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var sdkCallback: Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLauncher = registerForActivityResult(
            MeasureCameraContract()
        ) { assessment: AssessmentEntity? ->
            //your code to handle SDK result
            if (assessment != null) {
                sdkCallback?.invoke(assessment.toString())
            } else {
                sdkCallback?.invoke("SDK no results")
            }
            sdkCallback = null
        }

        (application as MainApplication).setActivityResultLauncher(activityResultLauncher)
    }

    fun setSDKCallback(callback: Callback) {
        this.sdkCallback = callback
    }
}
