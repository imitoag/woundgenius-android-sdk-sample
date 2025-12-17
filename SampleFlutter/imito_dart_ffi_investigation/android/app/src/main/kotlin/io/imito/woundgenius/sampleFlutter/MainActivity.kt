package io.imito.woundgenius.sampleFlutter

import android.content.Intent
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods
import io.imito.woundgenius.sdk.ui.screen.bodypicker.BodyPickerActivity
import io.imito.woundgenius.sdk.ui.screen.measurecamera.MeasureCameraActivity
import io.imito.woundgenius.sdk.ui.screen.support.MeasureSupportActivity
import java.io.File

class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                if (call.method.equals("openCamera")) {
                    openSdkCameraScreen()
                } else if (call.method.equals("openBodyPicker")) {
                    openSdkBodyPickerScreen()
                } else if (call.method.equals("openHelpScreen")) {
                    openSdkHelpScreen()
                } else {
                    result.notImplemented()
                }
            }
    }


    private fun openSdkCameraScreen() {
        val previewDir = File(this@MainActivity.cacheDir, "sdkImages")
        if (!previewDir.exists()) {
            previewDir.mkdir()
        }
        MeasureCameraActivity.openForActivityResult(
            activity = this@MainActivity,
            mediaFolder = previewDir.absolutePath, //change the directory to whatever you want
            SDK_CAMERA_REQUEST_CODE
        )
    }

    private fun openSdkBodyPickerScreen() {
        BodyPickerActivity.open(
            this@MainActivity, SDK_BODY_PICKER_REQUEST_CODE, null, null, null
        )
    }

    private fun openSdkHelpScreen() {
        MeasureSupportActivity.open(
            this@MainActivity,
            CameraMods.MARKER_DETECT_MODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                SDK_CAMERA_REQUEST_CODE -> {
                    val result =
                        data?.let { (it.getSerializableExtra(MeasureCameraActivity.KEY_RES_ARGS) as MeasureCameraActivity.Companion.ResArgs).assessment }
                    Log.d("woundGeniusResult", result.toString())
                }
                SDK_BODY_PICKER_REQUEST_CODE -> {
//                    val result =
//                        data?.let { (it.getSerializableExtra(BodyPickerActivity.KEY_RES_ARGS) as BodyPickerActivity.Companion.ResArgs).bodyParts }
//                    Log.d("woundGeniusResult", result.toString())
                }
            }
        }
    }

    companion object {
        private const val CHANNEL = "your_sdk_channel"
        private const val SDK_CAMERA_REQUEST_CODE = 0
        private const val SDK_BODY_PICKER_REQUEST_CODE = 1
    }
}
