package io.imito.woundgenius.sample.data.pojo.license

import io.imito.woundgenius.sdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods

data class SdkFeatureStatus(
    val availableModes: List<CameraMods>,
    val isMultipleOutlinesSupported: Boolean,
    val isStomaFlowEnable: Boolean,
    val autoDetectionMode: WoundAutoDetectionMode,
    val maxNumberOfMedia: Int,
    val isLiveDetectionEnabled: Boolean?,
    val isMediaFromGalleryAllowed: Boolean,
    val isBodyPickerAllowed: Boolean,
    val isMeasurementLineEnabled: Boolean,
    val isSingleAreaEnabled: Boolean,
    val isFrontalCameraSupported: Boolean,
    val isLandScapeSupported: Boolean
)
