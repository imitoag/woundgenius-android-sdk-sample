package com.example.samplewoundsdk.ui.screen.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.usecase.license.GetLicenseKeyUseCase
import com.example.samplewoundsdk.data.usecase.license.SaveLicenseKeyUseCase
import com.example.samplewoundsdk.data.usecase.user.GetUserIdUseCase
import com.example.samplewoundsdk.data.usecase.user.SaveUserIdUseCase
import com.example.samplewoundsdk.ui.screen.base.AbsViewModel
import com.example.woundsdk.data.pojo.license.LicenseErrorType
import com.example.woundsdk.data.pojo.license.LicenseValidateResult
import com.example.woundsdk.di.WoundGeniusSDK
import javax.inject.Inject

class SettingsScreenViewModel @Inject constructor(
    private val saveLicenseKeyUseCase: SaveLicenseKeyUseCase,
    private val saveUserIdUseCase: SaveUserIdUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getLicenseKeyUseCase: GetLicenseKeyUseCase
    ) : AbsViewModel() {

    private val _primaryButtonColorList = MutableLiveData(primaryColorList)
    val primaryButtonColorList: LiveData<List<Pair<String, Int>>>
        get() = _primaryButtonColorList

    private val _lightBGColorListLD = MutableLiveData(lightBGList)
    val lightBGColorListLD: LiveData<List<Pair<String, Int>>>
        get() = _lightBGColorListLD

    private val _userIdLD= MutableLiveData<String>()
    val userIdLD: LiveData<String>
        get() = _userIdLD

    private val _licenseErrorDialog = MutableLiveData<Triple<Boolean, LicenseErrorType?, Unit?>>()
    val licenseErrorDialog: LiveData<Triple<Boolean, LicenseErrorType?, Unit?>>
        get() = _licenseErrorDialog

    private val _licenseError = MutableLiveData<Unit?>()
    val licenseError: LiveData<Unit?>
        get() = _licenseError

    private val _onSavedLicenseKeyReceived = MutableLiveData<Unit?>()
    val onSavedLicenseKeyReceived: LiveData<Unit?>
        get() = _onSavedLicenseKeyReceived


    private val _availableFeatures = MutableLiveData<List<String>>()
    val availableFeatures: LiveData<List<String>>
        get() = _availableFeatures


    private val _noLicenseKeyErrorDialog = MutableLiveData<Unit?>()
    val noLicenseKeyErrorDialog: LiveData<Unit?>
        get() = _noLicenseKeyErrorDialog

    private fun openLicenseIssueDialog(dialogType: LicenseErrorType?) {
        _licenseErrorDialog.value = Triple(true, dialogType, Unit)
        _licenseErrorDialog.value = Triple(true, dialogType, null)
    }

    private fun openNoLicenseKeyDialog() {
        _noLicenseKeyErrorDialog.value = Unit
        _noLicenseKeyErrorDialog.value = null
    }

    private fun onLicenseError(){
        _licenseError.value = Unit
        _licenseError.value = null
    }

    fun getLicenseKey() {
        val params = GetLicenseKeyUseCase.Params.forGetLicenseKey()
        add(
            getLicenseKeyUseCase.execute(params)
                .subscribe({
                        WoundGeniusSDK.setLicenseKey(it)
                    _onSavedLicenseKeyReceived.value = Unit
                    _onSavedLicenseKeyReceived.value = null
                }, {

                })
        )
    }

    fun handleLicenseResult(result: LicenseValidateResult) {
        if (result is LicenseValidateResult.onError) {
            when (result.info) {
                LicenseErrorType.NO_LICENSE_KEY.value -> {
                    onLicenseError()
                    openNoLicenseKeyDialog()
                }

                LicenseErrorType.ILLEGALLY_MODIFIED.value -> {
                    onLicenseError()
                    openLicenseIssueDialog(LicenseErrorType.ILLEGALLY_MODIFIED)
                }

                LicenseErrorType.FAILED_TO_ENCODE_CONTENT.value -> {
                    onLicenseError()
                    openLicenseIssueDialog(LicenseErrorType.FAILED_TO_ENCODE_CONTENT)
                }

                LicenseErrorType.CORRUPTED_DATE_FORMAT.value -> {
                    onLicenseError()
                    openLicenseIssueDialog(LicenseErrorType.CORRUPTED_DATE_FORMAT)
                }

                LicenseErrorType.LICENSE_EXPIRED.value -> {
                    onLicenseError()
                    openLicenseIssueDialog(LicenseErrorType.LICENSE_EXPIRED)
                }

                LicenseErrorType.APP_ID_IS_LOCKED.value -> {
                    onLicenseError()
                    openLicenseIssueDialog(LicenseErrorType.APP_ID_IS_LOCKED)
                }

                LicenseErrorType.NO_UNLOCKED_FEATURES.value -> {
                    onLicenseError()
                    openLicenseIssueDialog(LicenseErrorType.NO_UNLOCKED_FEATURES)
                }
            }
        } else {
            if (result is LicenseValidateResult.onSuccess) {
                _availableFeatures.value = result.features
                _licenseErrorDialog.value = Triple(false, null, null)
            }
        }
    }

    fun saveLicenseKey(license: String) {
        val params = SaveLicenseKeyUseCase.Params.forSaveLicenseKey(license)
        add(
            saveLicenseKeyUseCase.execute(params)
                .subscribe({

                }, {

                })
        )
    }

    fun saveUserId(userId: String) {
        val params = SaveUserIdUseCase.Params.forSaveUserId(userId)
        add(
            saveUserIdUseCase.execute(params)
                .subscribe({

                }, {

                })
        )
    }

    fun getUserId(){
        val params = GetUserIdUseCase.Params.forGetUserId()
        add(
            getUserIdUseCase.execute(params)
                .subscribe({
                    _userIdLD.value = it
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
