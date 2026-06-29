package io.imito.woundgenius.sample.ui.screen.main


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.core.view.WindowInsetsControllerCompat
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.databinding.SampleAppActivityMainBinding
import io.imito.woundgenius.sample.ui.screen.base.AbsActivity
import io.imito.woundgenius.sample.ui.screen.homescreen.HomeScreenFragment
import io.imito.woundgenius.sample.ui.screen.settings.SettingsScreenFragment
import io.imito.woundgenius.sdk.internal.data.pojo.mode.OperatingMode
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.internal.ui.screen.whatsnew.WhatsNewActivity
import io.imito.woundgenius.sdk.internal.utils.system.DarkModeUtils
import java.lang.reflect.Field
import java.util.Locale


class MainActivity : AbsActivity<MainViewModel>(), MainBridge {

    override fun provideViewModelClass() = MainViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_activity_main

    private lateinit var binding: SampleAppActivityMainBinding

    override fun initListeners() {
        // No-op: this activity sets up its observers in onCreate and has no listeners to register here
    }


    private var currentFragmentTag: String = HOME_FRAGMENT_TAG

    private fun getAllLocalizedStrings(context: Context, locale: Locale): HashMap<String, String> {
        val localizedStrings = HashMap<String, String>()

        // Get current Resources and Configuration
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedResources = context.createConfigurationContext(config).resources

        // Use reflection to access all string resource IDs
        try {
            val fields: Array<Field> = R.string::class.java.declaredFields
            for (field in fields) {
                val resourceId = field.getInt(null)  // Get the resource ID
                val key = field.name  // Get the key (name of the resource)
                val localizedValue = localizedResources.getString(resourceId)  // Get the localized value
                localizedStrings[key] = localizedValue  // Add the key-value pair to the HashMap
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return localizedStrings
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_FRAGMENT_TAG, currentFragmentTag)
    }

    override fun onCreate(savedInstanceState: Bundle?) { // NOSONAR Cognitive Complexity — Activity/UI lifecycle code, refactor requires on-device verification
        super.onCreate(savedInstanceState)
        binding = SampleAppActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG, HOME_FRAGMENT_TAG)
        }

        viewModel?.apply {

            getUserId()
            userIdLD.observe(this@MainActivity) { userId ->

                WoundGeniusSDK.setCustomerUserId(userId)
                val existingFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
                if (WoundGeniusSDK.getConfiguration().operatingMode == OperatingMode.SDK) {

                    val isPresent = checkIfWhatsNewPresent()

                    if (!isPresent) {
                        if (existingFragment == null) {
                            this@MainActivity.openHomeScreen()
                        }
                    } else {
                        openHomeScreen()
                    }
                } else {
                    if (existingFragment == null) {
                        this@MainActivity.openHomeScreen()
                    }
                }
            }

            showWhatNewScreenLD.observe(this@MainActivity) { show ->
                show ?: return@observe

                val localization = getAllLocalizedStrings(this@MainActivity,Locale(getString(R.string.WOUND_GENIUS_SDK_LANGUAGE_CODE)) )

                val currentAppVersion = WoundGeniusSDK.extractVersion(WoundGeniusSDK.sdkReleaseVersion.substringBefore("-") ,3)
                val whatsNewFile = WHATS_NEW_ZIP_FILE_PATTERN.replace("$","3.3.0")
                WhatsNewActivity.open(
                    context = this@MainActivity,
                    whatNewContentPath = whatsNewFile,
                    localization = localization
                )
                viewModel?.setWhatNewScreenShowed()
            }


            openHomeScreenLD.observe(this@MainActivity) {

                it ?: return@observe
                val existingFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
                if (existingFragment == null) {

                    this@MainActivity.openHomeScreen()
                }
            }
        }
    }

    private fun checkIfWhatsNewPresent(): Boolean {
        val currentAppVersion = WoundGeniusSDK.sdkReleaseVersion.substringBefore("-")
        val whatsNewFile = WHATS_NEW_ZIP_FILE_PATTERN.replace("$",currentAppVersion)
        return assets.list("")?.contains(whatsNewFile) ?: false
    }

    override fun onResume() {
        super.onResume()
        val fragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)
        if (fragment == null) {
            if (viewModel?.whatNewScreenIsShowed?.value == true) {
                viewModel?.resetWhatNewScreenShowed()
                openHomeScreen()
            }
        }
        val window = this.window
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars = !DarkModeUtils.isDarkModeEnabled(this@MainActivity)
    }


    override fun onKeyboardOpen() { /* No-op: this screen does not react to the keyboard opening */ }

    override fun onKeyboardClose() { /* No-op: this screen does not react to the keyboard closing */ }

    private fun openHomeScreen(){
        val fragment = HomeScreenFragment.newInstance()
        currentFragmentTag = HOME_FRAGMENT_TAG

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFl, fragment, currentFragmentTag)
            .commit()
    }


    override fun openSettingsScreen() {
        val fragment = SettingsScreenFragment.newInstance()
        currentFragmentTag = SETTINGS_FRAGMENT_TAG

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFl, fragment,currentFragmentTag)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    companion object {
        private const val WHATS_NEW_ZIP_FILE_PATTERN = "WhatsNew$.zip"
        private const val HOME_FRAGMENT_TAG = "HOME_SCREEN_TAG"
        private const val SETTINGS_FRAGMENT_TAG = "SETTINGS_FRAGMENT_TAG"
        private const val CURRENT_FRAGMENT_TAG = "current_fragment_tag"

        fun open(context: Context) =
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
    }

}
