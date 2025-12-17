package io.ionic.starter

import android.app.Application
import android.content.pm.PackageManager
import io.imito.woundgenius.sdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods
import io.imito.woundgenius.sdk.di.WoundGeniusSDK


class WoundGeniusSdkManager {
  fun init(application: Application) {

    WoundGeniusSDK.init(application, "")

    WoundGeniusSDK.configure(
      availableModes = listOf(
        CameraMods.MARKER_DETECT_MODE,
        CameraMods.MANUAL_MEASURE_MODE,
        CameraMods.PHOTO_MODE
      ),
      defaultMode = CameraMods.MARKER_DETECT_MODE,
      woundAutoDetectionMode = WoundAutoDetectionMode.WOUND,
      isLiveDetectionEnabled = true,
      isAddFromLocalStorageAvailable = true,
      isAddBodyPickerOnCaptureScreenAvailable = false,
      isFrontCameraUsageAllowed = application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT),
      isFullHDVideoEnabled = false,
      isCancelBarButtonItemVisible = true,
      maxNumberOfMedia = 100,
      isRightNavBarButtonAvailable = true,
      completionButtonTitle = null,
      isDepthInputEnabled = true,
      showTotalCircumference = true,
      isSendPrintablePDFHidden = false,
      isResultsBottomBarHidden = false,
      maxNumberOfCalibrationMedia = 1,
      isMultipleOutlinesEnabled = true,
      isMinNumberOfMediaRequired = false,
      isStomaFlow = false,
      lightBackgroundColor = null,
      isMeasurementLineEnabled = true
    )

  }
}
