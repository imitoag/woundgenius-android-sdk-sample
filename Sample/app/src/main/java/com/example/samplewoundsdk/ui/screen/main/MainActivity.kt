package com.example.samplewoundsdk.ui.screen.main


import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleAppActivityMainBinding
import com.example.samplewoundsdk.ui.screen.homescreen.HomeScreenFragment
import com.example.samplewoundsdk.ui.screen.base.AbsActivity
import com.example.samplewoundsdk.ui.screen.settings.SettingsScreenFragment
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
            val fragment = HomeScreenFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainFl, fragment)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onKeyboardOpen() {}

    override fun onKeyboardClose() {}

    companion object {
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
