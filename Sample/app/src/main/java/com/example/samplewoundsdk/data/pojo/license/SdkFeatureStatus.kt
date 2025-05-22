package com.example.samplewoundsdk.data.pojo.license

import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.data.pojo.camera.cameramod.CameraMods
import com.example.woundsdk.data.pojo.camera.rotation.WoundGeniusOrientationMods

data class SdkFeatureStatus(
    val availableModes: List<CameraMods>,
    val isMultipleOutlinesSupported: Boolean,
    val isStomaFlowEnable: Boolean,
    val autoDetectionMode: WoundAutoDetectionMode,
    val maxNumberOfMedia: Int,
    val isLiveDetectionEnabled: Boolean?,
    val isMediaFromGalleryAllowed: Boolean,
    val isBodyPickerAllowed: Boolean,
    val isFrontalCameraSupported: Boolean,
    val isLandScapeSupported: Boolean
)
