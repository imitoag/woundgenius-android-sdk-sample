package com.example.samplewoundsdk.ui.screen.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.samplewoundsdk.data.usecase.license.GetLicenseKeyUseCase
import com.example.samplewoundsdk.data.usecase.user.GetUserIdUseCase
import com.example.samplewoundsdk.ui.screen.base.AbsViewModel
import com.example.woundsdk.di.WoundGeniusSDK
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getLicenseKeyUseCase: GetLicenseKeyUseCase
) : AbsViewModel(

) {


    init {
        getLicenseKey()
    }

    private val _showWhatNewScreenLD = MutableLiveData<Unit?>()
    val showWhatNewScreenLD: LiveData<Unit?>
        get() = _showWhatNewScreenLD


    private val _openHomeScreenLD = MutableLiveData<Unit?>()
    val openHomeScreenLD: LiveData<Unit?>
        get() = _openHomeScreenLD

    private val _whatNewScreenIsShowed = MutableLiveData(false)
    val whatNewScreenIsShowed: LiveData<Boolean>
        get() = _whatNewScreenIsShowed

    private val _userIdLD= MutableLiveData<String>()
    val userIdLD: LiveData<String>
        get() = _userIdLD


    private fun getLicenseKey() {
        val params = GetLicenseKeyUseCase.Params.forGetLicenseKey()
        add(
            getLicenseKeyUseCase.execute(params)
                .subscribe({
                    if (it.isNotEmpty()) {
                        WoundGeniusSDK.setLicenseKey(it)
                    }
                }, {

                })
        )
    }

    fun resetWhatNewScreenShowed() {
        _whatNewScreenIsShowed.value = false
    }

    fun setWhatNewScreenShowed(){
        _whatNewScreenIsShowed.value = true
    }

    fun openWhatNewScreen(){
        _showWhatNewScreenLD.postValue(Unit)
    }
    fun openHomeScreen(){
        _openHomeScreenLD.postValue(Unit)
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
}
