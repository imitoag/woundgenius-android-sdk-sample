package com.example.samplewoundsdk.ui.screen.main


import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleAppActivityMainBinding
import com.example.samplewoundsdk.ui.screen.homescreen.HomeScreenFragment
import com.example.samplewoundsdk.ui.screen.base.AbsActivity
import com.example.samplewoundsdk.ui.screen.settings.SettingsScreenFragment
import com.example.woundsdk.ui.screen.whatsnew.WhatsNewActivity
import com.example.woundsdk.data.pojo.WoundGeniusOperatingMode
import com.example.woundsdk.di.WoundGeniusSDK


class MainActivity : AbsActivity<MainViewModel>(), MainBridge {

    override fun provideViewModelClass() = MainViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_activity_main

    lateinit var binding: SampleAppActivityMainBinding

    override fun initListeners() {

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
                    val htmlWebViewValue = checkIfWhatsNewPresent()

                    if (htmlWebViewValue == null) {
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
                val htmlWebViewValue = checkIfWhatsNewPresent()
                val cssPartOneValue = try {
                    getString(
                        resources.getIdentifier(
                            CSS_KEY_FIRST_KEY,
                            "string",
                            packageName
                        )
                    )
                } catch (e: Exception) {
                    null
                }

                val cssPartTwoValue = try {
                    getString(
                        resources.getIdentifier(
                            CSS_KEY_SECOND_KEY,
                            "string",
                            packageName
                        )
                    )
                } catch (e: Exception) {
                    null
                }

                val cssWhatsNewValue = try {
                    getString(
                        resources.getIdentifier(
                            WHATS_NEW_CSS,
                            "string",
                            packageName
                        )
                    )
                } catch (e: Exception) {
                    null
                }

                WhatsNewActivity.open(
                    this@MainActivity,
                    htmlWebViewValue ?: "",
                    cssPartOneValue = cssPartOneValue,
                    cssPartTwoValue = cssPartTwoValue,
                    cssWhatsNewValue = cssWhatsNewValue
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

    private fun checkIfWhatsNewPresent(): String? {
        val currentAppVersion = WoundGeniusSDK.sdkReleaseVersion.substringBefore("-")
        val androidNotesKeyName = "WHATS_NEW_${currentAppVersion}_Android_HTML"
        val generalNotesKeyName = "WHATS_NEW_${currentAppVersion}_HTML"
        val htmlWebViewValue = try {
            getString(
                resources.getIdentifier(
                    androidNotesKeyName,
                    "string",
                    packageName
                )
            )
        } catch (e: Exception) {
            try {
                getString(
                    resources.getIdentifier(
                        generalNotesKeyName,
                        "string",
                        packageName
                    )
                )
            } catch (e: Exception) {
                null
            }
        }
        return htmlWebViewValue
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
        private const val CSS_KEY_FIRST_KEY = "COMMON_CSS_PART1"
        private const val CSS_KEY_SECOND_KEY = "COMMON_CSS_PART2"
        private const val WHATS_NEW_CSS = "WHATS_NEW_CSS"
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
