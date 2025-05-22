package com.awesomeproject

import android.app.Application
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.example.woundsdk.di.WoundGeniusSDK
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader

class MainApplication : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> =
                PackageList(this).packages.apply {
                    // Packages that cannot be autolinked yet can be added manually here, for example:
                    // add(MyReactNativePackage())
                    add(MySdkPackage())
                }

            override fun getJSMainModuleName(): String = "index"

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    private var _activityResultLauncher: ActivityResultLauncher<Intent>? = null

    fun setActivityResultLauncher(launcher: ActivityResultLauncher<Intent>) {
        this._activityResultLauncher = launcher
    }

    fun getActivityResultLauncher(): ActivityResultLauncher<Intent>? {
        return _activityResultLauncher
    }

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            load()
        }
        var licenseKey =
            "eyJlbmMiOiJleUprWVhSaElqcDdmU3dpYldWMFlTSTZleUpwYzNOMVpXUWlPaUl5TURJMExUQTRMVEV6SURBNE9qRXpPakE0SWl3aVpYaHdhWEo1SWpvaU1qQXlOUzB3T0MweE15QXdPRG94TXpvd01DSjlMQ0pwYm1Oc2RXUmxaQ0k2VzNzaWRIbHdaU0k2SW1Gd2NHeHBZMkYwYVc5dVNXUWlMQ0pwWkNJNkltTnZiUzVoZDJWemIyMWxjSEp2YW1WamRDSjlMSHNpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0lzSW1sa0lqb2ljR2h2ZEc5RFlYQjBkWEpwYm1jaWZTeDdJbWxrSWpvaWRtbGtaVzlEWVhCMGRYSnBibWNpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW5KMWJHVnlUV1ZoYzNWeVpXMWxiblJEWVhCMGRYSnBibWNpTENKMGVYQmxJam9pWm1WaGRIVnlaVWxrSW4wc2V5SnBaQ0k2SW0xaGNtdGxjazFsWVhOMWNtVnRaVzUwUTJGd2RIVnlhVzVuSWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKbWNtOXVkR0ZzUTJGdFpYSmhJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWRIbHdaU0k2SW1abFlYUjFjbVZKWkNJc0ltbGtJam9pYlhWc2RHbHdiR1ZYYjNWdVpITlFaWEpKYldGblpTSjlMSHNpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0lzSW1sa0lqb2lkMjkxYm1SRVpYUmxZM1JwYjI0aWZTeDdJbWxrSWpvaWJHbDJaVmR2ZFc1a1JHVjBaV04wYVc5dUlpd2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSjlMSHNpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0lzSW1sa0lqb2lZbTlrZVZCaGNuUlFhV05yWlhJaWZTeDdJbWxrSWpvaWJHOWpZV3hUZEc5eVlXZGxTVzFoWjJWeklpd2lkSGx3WlNJNkltWmxZWFIxY21WSlpDSjlMSHNpYVdRaU9pSnNiMk5oYkZOMGIzSmhaMlZXYVdSbGIzTWlMQ0owZVhCbElqb2labVZoZEhWeVpVbGtJbjBzZXlKMGVYQmxJam9pWm1WaGRIVnlaVWxrSWl3aWFXUWlPaUowYVhOemRXVlVlWEJsUkdWMFpXTjBhVzl1SW4wc2V5SjBlWEJsSWpvaVptVmhkSFZ5WlVsa0lpd2lhV1FpT2lKemRHOXRZVVJ2WTNWdFpXNTBZWFJwYjI0aWZTeDdJbWxrSWpvaVltRnlZMjlrWlZOallXNXVhVzVuSWl3aWRIbHdaU0k2SW1abFlYUjFjbVZKWkNKOUxIc2lhV1FpT2lKdFlXNTFZV3hOWldGemRYSmxiV1Z1ZEVsdWNIVjBJaXdpZEhsd1pTSTZJbVpsWVhSMWNtVkpaQ0o5TEhzaWRIbHdaU0k2SW1abFlYUjFjbVZKWkNJc0ltbGtJam9pYUdGdVpIbHpZMjl3WlVOaGNIUjFjbWx1WnlKOVhYMD0iLCJzaWciOiJGRXlOeFV6REVySkZSdVIxd1wvb3IrcFBVSHA2WGVzSDZoeFFkQVFOaTYyTnhJQjdnVnVpZ09uSVdqbzVXVTRDOTZIc2dzS0EyMTB4MlpIRm93ZFo2ZTNhRFNqUGQwMFBNT1wvcWQ2Z3VIR1lqZDNMS04xUUVPYWlIRURHdWlkbEJmc2pNMjc4WE9sbjVLZGdvMFwvVjZ3d3NmcVNheXVjc0I0a2J4XC83SEJrcldpVVlIVmZyaWRiNmFRaVpBUUFjb2hCb3NEVzZlSGtlanB0Y1JDeUVjMUY1eWlBbXNGdXpVUWVEV1g5Y016eGgxWGNMTG5oR2NcL2ZtU054NTd1dEdKUE1USjU3UFdDVTdqTEwyTFc5WmhDUDZWUUFweXpwTnhsVjlZeVJYa3dHaDZmemMrb3N0ZmpzTERWbWVvQ3U4cUF3OVk3NUFONGVUUzA4d1V0R0txSlZQOTVaK0tvbm02cmFxMk1cL1Y5ZUNXNVZxRXZYaXNsY1oxa1wvMmp1RjV3NlVFU3E2YkQ1QU5hUnFBdlR4NzJzZ2N3Q2RsYnozNFdzZ1gwelB1RlBoNGlWeWZYWFdjeXRFS2Y3OGFzR3pVMjYyY2FYUEFFRjBKd0xyMnJMRHJud3F6VmRQcWJLMFwvTGV1elZZN0VvMXVkRENuWkVVdTg3NHdUVHJ1dm1QQ1loUm5qNUtxbkhMZGZ0d0d3RnNvN05pY0NpWmNlUGlvMkhiSjdoRXc3ZWhTR0ZcL1NLbnpYK2gwN1hmSGNXMGlXNkJtbTN0ZW9qZEdNWmNWbDUzNGx2aEx1OStrS1N1emhUaEVkUTFTUkZsRlByQWZ0OE1rc1pieFR2TUZzMzlYcXJOaXcxWU9VbWRrYWI3cUVrbGp5OHFVRVhHeHhaSDNyQzJtS00zUzFNVFJ4T1hiUT0iLCJhbGciOiIxIn0="

        WoundGeniusSDK.init(
            application = this,
            appBundleId = BuildConfig.APPLICATION_ID,
            licenseKey = licenseKey
        )

    }
}
