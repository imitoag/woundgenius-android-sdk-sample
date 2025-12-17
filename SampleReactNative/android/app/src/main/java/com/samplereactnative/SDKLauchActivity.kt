package com.samplereactnative

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import io.imito.woundgenius.sdk.data.pojo.assessment.entity.AssessmentEntity
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods
import io.imito.woundgenius.sdk.ui.screen.bodypicker.BodyPartContract
import io.imito.woundgenius.sdk.ui.screen.bodypicker.BodyPickerActivity.Companion.openWithResult
import io.imito.woundgenius.sdk.ui.screen.measurecamera.MeasureCameraActivity
import io.imito.woundgenius.sdk.ui.screen.measurecamera.MeasureCameraContract
import io.imito.woundgenius.sdk.ui.screen.support.MeasureSupportActivity
import java.io.File

class SDKLauchActivity : AppCompatActivity() {

//    lateinit var binding: SDKLaunchActivityBinding

    private val measureCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        MeasureCameraContract()
    ) { assessment: AssessmentEntity? ->
        if (assessment != null) {

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
//            MeasureSupportActivity.open(
//                this@MainActivity,
//                CameraMods.MARKER_DETECT_MODE
//            )
//        })
    }
}
