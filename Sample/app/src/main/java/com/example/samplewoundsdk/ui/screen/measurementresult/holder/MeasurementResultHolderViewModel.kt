package com.example.samplewoundsdk.ui.screen.measurementresult.holder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.samplewoundsdk.ui.screen.base.AbsViewModel
import javax.inject.Inject

class MeasurementResultHolderViewModel @Inject constructor(

) : AbsViewModel() {

    private val _response = MutableLiveData<Unit>()
    val response: LiveData<Unit>
        get() = _response

}
