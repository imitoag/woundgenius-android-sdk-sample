package io.ionic.starter

import android.app.Application
import android.content.pm.PackageManager
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.api.models.configuration.WoundGeniusConfiguration
import io.imito.woundgenius.sdk.api.models.presenter.WGPresenter
import io.imito.woundgenius.sdk.internal.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode


class WoundGeniusSdkManager {
  fun init(application: Application) {

    WoundGeniusSDK.init(application, "")

    WoundGeniusSDK.configure(
      presenter = WGPresenter(
        configuration = WoundGeniusConfiguration(
          availableModes = listOf(
            ImitoCameraMode.MARKER_DETECT_MODE,
            ImitoCameraMode.MANUAL_MEASURE_MODE,
            ImitoCameraMode.PHOTO_MODE
          ),
          defaultMode = ImitoCameraMode.MARKER_DETECT_MODE,
          autoDetectionMode = WoundAutoDetectionMode.WOUND,
          isLiveWoundDetectionEnabled = true,
          isAddFromLocalStorageAvailable = true,
          isBodyPartPickerAvailable = false,
          isFrontCameraUsageAllowed = application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT),
          isFullHDVideoEnabled = false,
          isCancelBarButtonItemVisible = true,
          maxNumberOfMedia = 100,
          isDepthOrHeightInputEnabled = true,
          showTotalCircumference = true,
          maxNumberOfCalibrationMedia = 1,
          isMultipleOutlinesEnabled = true,
          minNumberOfMedia = 0,
          isStomaFlow = false,
          lightBackgroundColor = null,
          isMeasurementLineEnabled = true
        )
      )
    )

  }
}
