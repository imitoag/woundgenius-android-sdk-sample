package io.imito.woundgenius.sampleFlutter

import android.app.Application
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.api.models.configuration.WoundGeniusConfiguration
import io.imito.woundgenius.sdk.api.models.presenter.WGPresenter

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        WoundGeniusSDK.init(
            application = this,
            licenseKey = ""
        )

        WoundGeniusSDK.configure(
            presenter = WGPresenter(
                configuration = WoundGeniusConfiguration(
                    isMeasurementLineEnabled = true
                )
            )
        )
    }
}