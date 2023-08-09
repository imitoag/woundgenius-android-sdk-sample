package com.example.samplewoundsdk.ui.screen.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.usecase.license.SaveLicenseKeyUseCase
import com.example.samplewoundsdk.ui.screen.base.AbsViewModel
import com.example.woundsdk.data.usecase.measurement.GetSizeUseCase
import com.example.woundsdk.di.WoundGeniusSDK
import javax.inject.Inject

class SettingsScreenViewModel @Inject constructor(
    private val saveLicenseKeyUseCase: SaveLicenseKeyUseCase
    ) : AbsViewModel() {

    private val _primaryButtonColorList = MutableLiveData(primaryColorList)
    val primaryButtonColorList: LiveData<List<Pair<String, Int>>>
        get() = _primaryButtonColorList

    private val _lightBGColorList = MutableLiveData(lightBGList)
    val lightBGColorList: LiveData<List<Pair<String, Int>>>
        get() = _lightBGColorList

    fun saveLicenseKey(license: String) {
        val params = SaveLicenseKeyUseCase.Params.forSaveLicenseKey(license)
        add(
            saveLicenseKeyUseCase.execute(params)
                .subscribe({

                }, {

                })
        )
    }


    companion object {
        private val primaryColorList = listOf(
            Pair("imitoRed", R.color.sample_app_red),
            Pair("Blue", R.color.sample_app_color_blue),
            Pair("Green", R.color.sample_app_color_green)
        )
        private val lightBGList = listOf(
            Pair("white", R.color.sample_app_white),
            Pair("lightGray", R.color.sample_app_color_text_gray_light),
            Pair("yellow", R.color.sample_app_color_yellow)
        )
    }
}
