package io.ionic.starter

import android.app.Application
import android.content.pm.PackageManager
import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.data.pojo.cameramod.CameraMods
import com.example.woundsdk.di.WoundGeniusSDK

class WoundGeniusSdkManager {
  fun init(application: Application) {

    WoundGeniusSDK.init(application, "io.ionic.starter", "licenseKey")

    WoundGeniusSDK.configure(
      availableModes = listOf(
        CameraMods.VIDEO_MODE,
        CameraMods.PHOTO_MODE,
        CameraMods.MARKER_DETECT_MODE
      ),
      defaultMode = CameraMods.PHOTO_MODE,
      woundAutoDetectionMode = WoundAutoDetectionMode.WOUND,
      isAddFromLocalStorageAvailable = true,
      isAddBodyPickerOnCaptureScreenAvailable = false,
      isFrontCameraUsageAllowed = application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT),
      maxNumberOfMedia = 100,
      isLiveDetectionEnabled = true,
      completionButtonTitle = null,
      isMultipleOutlinesEnabled = false,
      isMinNumberOfMediaRequired = true,
      isStomaFlow = false,
      lightBackgroundColor = null,
    )
  }
}
