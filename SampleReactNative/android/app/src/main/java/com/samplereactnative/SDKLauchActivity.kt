package com.samplereactnative

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.MeasurementResult
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.internal.ui.screen.bodypicker.BodyPartContract
import io.imito.woundgenius.sdk.internal.ui.screen.bodypicker.BodyPickerActivity.Companion.openWithResult
import io.imito.woundgenius.sdk.internal.ui.screen.measurecamera.MeasureCameraActivity
import io.imito.woundgenius.sdk.internal.ui.screen.measurecamera.MeasureCameraContract
import io.imito.woundgenius.sdk.internal.ui.screen.support.HelpScreenActivity
import java.io.File

class SDKLauchActivity : AppCompatActivity() {

//    lateinit var binding: SDKLaunchActivityBinding

    private val measureCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        MeasureCameraContract()
    ) { results: List<MeasurementResult>? ->
        if (results != null) {

        } else {
            finish()
        }
    }

    private val bodyPartLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        BodyPartContract()
    ) {
        if (it != null) {
            //your code....
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sdk_launch_activity)

        val previewDir = File(this@SDKLauchActivity.cacheDir, "sdkImages")
        if (!previewDir.exists()) {
            previewDir.mkdir()
        }
//
//        openCameraButtonACTV.setOnClickListener({ v ->
//            MeasureCameraActivity.openWithResult(
//                measureCameraLauncher,
//                this@SDKLauchActivity,  // Replaceable by "fragment = this" if using an fragment
//                previewDir.absolutePath
//            )
//        })
//
//        openBodyPickerButtonACTV.setOnClickListener({ v ->
//          BodyPickerActivity.openWithResult(
//                bodyPartLauncher,
//                this@SDKLauchActivity,  // Replaceable by "fragment = this" if using an fragment
//                null,
//                null,
//                null
//            )
//        })
//
//        openHelpScreenButtonACTV.setOnClickListener({ v ->
//            HelpScreenActivity.open(
//                this@MainActivity,
//                ImitoCameraMode.MARKER_DETECT_MODE
//            )
//        })
    }
}
