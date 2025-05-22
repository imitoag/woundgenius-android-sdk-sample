package com.example.samplewoundsdk.ui.screen.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.license.SdkFeatureStatus
import com.example.samplewoundsdk.data.usecase.license.GetSdkFeaturesUseCase
import com.example.samplewoundsdk.data.usecase.license.SaveLicenseKeyUseCase
import com.example.samplewoundsdk.data.usecase.license.SaveSdkFeaturesUseCase
import com.example.samplewoundsdk.data.usecase.user.GetUserIdUseCase
import com.example.samplewoundsdk.data.usecase.user.SaveUserIdUseCase
import com.example.samplewoundsdk.ui.screen.base.AbsViewModel
import com.example.woundsdk.data.pojo.license.LicenseValidateResult
import com.example.woundsdk.di.WoundGeniusSDK
import timber.log.Timber
import javax.inject.Inject

class SettingsScreenViewModel @Inject constructor(
    private val saveLicenseKeyUseCase: SaveLicenseKeyUseCase,
    private val saveSdkFeaturesUseCase: SaveSdkFeaturesUseCase,
    private val getSdkFeaturesUseCase: GetSdkFeaturesUseCase,
    private val saveUserIdUseCase: SaveUserIdUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : AbsViewModel() {

    private val _primaryColorListLD = MutableLiveData(primaryColorList)
    val primaryColorListLD: LiveData<List<Pair<String, Int?>>>
        get() = _primaryColorListLD

    private val _secondaryColorListLD = MutableLiveData(secondaryColorList)
    val secondaryColorListLD: LiveData<List<Pair<String, Int?>>>
        get() = _secondaryColorListLD


    private val _userIdLD = MutableLiveData<String>()
    val userIdLD: LiveData<String>
        get() = _userIdLD

    private val _newAvailableFeatures = MutableLiveData<List<String>>()
    val newAvailableFeatures: LiveData<List<String>>
        get() = _newAvailableFeatures

    private val _sdkFeaturesStatusLD = MutableLiveData<SdkFeatureStatus>()
    val sdkFeaturesStatusLD: LiveData<SdkFeatureStatus>
        get() = _sdkFeaturesStatusLD

    init {
        getFeatureStatus()
    }

    fun saveLicenseKey(license: String) {
        val params = SaveLicenseKeyUseCase.Params.forSaveLicenseKey(license)
        add(
            saveLicenseKeyUseCase.execute(params)
                .subscribe({
                    validateSDKCustomerLicense()
                }, {
                     Log.d("woundGeniusError", it.stackTraceToString())
                })
        )
    }

    fun saveFeatureStatus() {
        val params = SaveSdkFeaturesUseCase.Params.forSaveSdkFeatures()
        add(
            saveSdkFeaturesUseCase.execute(params)
                .subscribe({

                }, {
                     Log.d("woundGeniusError", it.stackTraceToString())
                })
        )
    }

    fun getFeatureStatus() {
        val params = GetSdkFeaturesUseCase.Params.forGetSdkFeatures()
        add(
            getSdkFeaturesUseCase.execute(params)
                .subscribe({
                    _sdkFeaturesStatusLD.value = it
                }, {
                     Log.d("woundGeniusError", it.stackTraceToString())
                })
        )
    }

    fun validateSDKCustomerLicense() {
        val licenseVerifyResult = WoundGeniusSDK.validateLicenseKey()

        if (licenseVerifyResult is LicenseValidateResult.onSuccess) {
            Log.d("settings","licenseVerifyResult onSuccess")

            _newAvailableFeatures.postValue(licenseVerifyResult.features)

        } else {
            Log.d("settings","licenseVerifyResult onFail")
            _newAvailableFeatures.postValue(emptyList())

        }
    }

    fun saveUserId(userId: String) {
        val params = SaveUserIdUseCase.Params.forSaveUserId(userId)
        add(
            saveUserIdUseCase.execute(params)
                .subscribe({

                }, {
                     Log.d("woundGeniusError", it.stackTraceToString())
                })
        )
    }

    fun getUserId() {
        val params = GetUserIdUseCase.Params.forGetUserId()
        add(
            getUserIdUseCase.execute(params)
                .subscribe({
                    _userIdLD.value = it
                }, {
                     Log.d("woundGeniusError", it.stackTraceToString())
                })
        )
    }

    companion object {
        private val primaryColorList = listOf(
            Pair("None", null),
            Pair("imitoRed", R.color.sample_app_red),
            Pair("Blue", R.color.sample_app_color_blue),
            Pair("Green", R.color.sample_app_color_green)
        )
        private val secondaryColorList = listOf(
            Pair("None", null),
            Pair("White", R.color.sample_app_white),
            Pair("Black", R.color.sample_app_color_black),
            Pair("Grey", R.color.sample_app_grey_dark)
        )
    }
}
