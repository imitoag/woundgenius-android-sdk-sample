package io.imito.woundgenius.sample.utils

import io.imito.woundgenius.sdk.di.WoundGeniusSDK
import io.imito.woundgenius.sdk.storage.shared.SharedMemory

/**
 * Re-applies the feature settings this app has persisted, so a restarted process runs with what the
 * user last chose rather than with the values compiled into the entry point.
 *
 * The SDK keeps its configuration in a Kotlin `object`, which dies with the process. It carries that
 * configuration across process death itself, but only for a restored SDK screen and only through the
 * saved instance state of that screen - there is nothing to restore from when the process comes back
 * up at the host's own launcher activity. From there the only configuration the SDK gets is whatever
 * the host passes on startup, so anything the host hard-codes silently overwrites the user's choice.
 *
 * The settings screen already treats storage as the source of truth: it pushes every one of these
 * values back into the SDK when it opens. This makes that happen from the first moment of the process
 * instead of only once the user has visited that screen.
 *
 * Note that clearing the app's cache does not remove any of this - these live in shared preferences.
 * What loses them is the process restart that clearing storage, or the system reclaiming memory from
 * a backgrounded app, brings with it.
 *
 * [maxNumberOfMedia][SharedMemory.getMaxNumberOfMedia] is deliberately absent: its stored default is
 * 1, which contradicts the 100 this app configures, so restoring it would cap a fresh install at a
 * single capture. Every other field's stored default matches the SDK's own.
 */
fun SharedMemory.restoreSdkFeatureSettings() {
    WoundGeniusSDK.configure(
        availableModes = getAvailableModes(),
        woundAutoDetectionMode = getAutoDetectionMode(),
        isLiveDetectionEnabled = getIsLiveDetectionEnabled(),
        isMultipleOutlinesEnabled = getIsMultipleOutlinesSupported(),
        isStomaFlow = getIsStomaFlowEnabled(),
        isAddFromLocalStorageAvailable = getIsMediaFromGalleryAllowed(),
        isAddBodyPickerOnCaptureScreenAvailable = getIsBodyPickerAllowed(),
        isFrontCameraUsageAllowed = getIsFrontalCameraSupported(),
        isLandScapeSupported = getIsLandScapeSupported(),
        isMeasurementLineEnabled = getIsMeasurementLineEnabled(),
        isSingleAreaEnabled = getIsSingleAreaEnabled()
    )
}
