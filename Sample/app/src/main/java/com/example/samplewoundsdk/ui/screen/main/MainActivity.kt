package com.example.samplewoundsdk.ui.screen.main


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleAppActivityMainBinding
import com.example.samplewoundsdk.ui.screen.base.AbsActivity
import com.example.samplewoundsdk.ui.screen.homescreen.HomeScreenFragment
import com.example.samplewoundsdk.ui.screen.settings.SettingsScreenFragment
import com.example.woundsdk.data.pojo.WoundGeniusOperatingMode
import com.example.woundsdk.di.WoundGeniusSDK
import com.example.woundsdk.ui.screen.whatsnew.WhatsNewActivity
import java.lang.reflect.Field
import java.util.Locale


class MainActivity : AbsActivity<MainViewModel>(), MainBridge {

    override fun provideViewModelClass() = MainViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_activity_main

    private lateinit var binding: SampleAppActivityMainBinding

    override fun initListeners() {

    }

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SampleAppActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel?.apply {
            getUserId()
            userIdLD.observe(this@MainActivity) { userId ->
                WoundGeniusSDK.setCustomerUserId(userId)
                if (WoundGeniusSDK.getWoundGeniusOperatingMode() == WoundGeniusOperatingMode.SDK) {
                    val isPresent = checkIfWhatsNewPresent()

                    if (!isPresent) {
                        val fragment = HomeScreenFragment.newInstance()
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.mainFl, fragment)
                            .commit()
                    } else {
                        val currentAppVersion = WoundGeniusSDK.sdkReleaseVersion
                        val needToShow = WoundGeniusSDK.showWhatsNewIfNeeded(
                            isNotePresent = true,
                            currentAppVersion = currentAppVersion
                        )
                        if (needToShow) {
                            openWhatNewScreen()
                        } else {
                            openHomeScreen()
                        }
                    }
                } else {
                    val fragment = HomeScreenFragment.newInstance()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.mainFl, fragment)
                        .commit()
                }
            }

            showWhatNewScreenLD.observe(this@MainActivity) { show ->
                show ?: return@observe

                val localization = getAllLocalizedStrings(this@MainActivity,Locale(getString(R.string.WOUND_GENIUS_SDK_LANGUAGE_CODE)) )
                val currentAppVersion = WoundGeniusSDK.sdkReleaseVersion.substringBefore("-")
                val whatsNewFile = WHATS_NEW_ZIP_FILE_PATTERN.replace("$",currentAppVersion)
                WhatsNewActivity.open(
                    context = this@MainActivity,
                    whatNewContentPath = whatsNewFile,
                    localization = localization
                )
                viewModel?.setWhatNewScreenShowed()
            }

            openHomeScreenLD.observe(this@MainActivity) {
                it ?: return@observe
                val fragment = HomeScreenFragment.newInstance()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainFl, fragment)
                    .commit()
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
        if (viewModel?.whatNewScreenIsShowed?.value == true) {
            viewModel?.resetWhatNewScreenShowed()
            val fragment = HomeScreenFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFl, fragment)
                .commit()
        }
    }


    override fun onKeyboardOpen() {}

    override fun onKeyboardClose() {}

    companion object {
        private const val WHATS_NEW_ZIP_FILE_PATTERN = "WhatsNew$.zip"

        fun open(context: Context) =
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
    }

    override fun openSettingsScreen() {
        val fragment = SettingsScreenFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFl, fragment)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }


}
