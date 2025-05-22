package com.awesomeproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraContract
import java.io.File

class SDKLauchActivity : AppCompatActivity() {

    private val measureCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        MeasureCameraContract()
    ) { assessment: AssessmentEntity? ->
        if (assessment != null) {
            //your code....
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sdk_launch_activity)

        val previewDir = File(this@SDKLauchActivity.cacheDir, "sdkImages")
        if (!previewDir.exists()) {
            previewDir.mkdir()
        }
        MeasureCameraActivity.openWithResult(
            launcher = measureCameraLauncher,
            activity = this@SDKLauchActivity,
            mediaFolder = previewDir.absolutePath //change the directory to whatever you want
        )
    }
}
