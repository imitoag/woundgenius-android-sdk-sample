package io.imito.woundgenius.sampleFlutter
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.ui.base.AbsDaggerActivity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraContract
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File

class MainActivity : FlutterActivity() {


    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
               if (call.method.equals("open")) {
                    openSdkScreen()
                } else {
                    result.notImplemented()
                }
            }
    }


    private fun openSdkScreen() {
        val previewDir = File(this@MainActivity.cacheDir, "sdkImages")
        if (!previewDir.exists()) {
            previewDir.mkdir()
        }
        MeasureCameraActivity.openForActivityResult(
            activity = this@MainActivity,
            mediaFolder = previewDir.absolutePath, //change the directory to whatever you want
            SDK_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SDK_REQUEST_CODE -> {
                    val result = data?.let { (it.getSerializableExtra(MeasureCameraActivity.KEY_RES_ARGS) as MeasureCameraActivity.Companion.ResArgs).assessment }
                    Log.d("woundGeniusResult",result.toString())
                }
            }
        }
    }

    companion object {
        private const val CHANNEL = "your_sdk_channel"
        private const val SDK_REQUEST_CODE = 0
    }
}
