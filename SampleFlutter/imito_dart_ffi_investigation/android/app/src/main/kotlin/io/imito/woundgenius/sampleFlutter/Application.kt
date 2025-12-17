package io.imito.woundgenius.sampleFlutter

import android.app.Application
import io.imito.woundgenius.sdk.di.WoundGeniusSDK
import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods
import io.imito.woundgenius.sdk.data.pojo.autodetectionmod.WoundAutoDetectionMode

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        WoundGeniusSDK.init(
            application = this,
            licenseKey = ""
        )

        WoundGeniusSDK.configure(isMeasurementLineEnabled = true)
    }
}