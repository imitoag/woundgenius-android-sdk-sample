package com.example.samplewoundsdk.ui.screen.measurementfullscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.samplewoundsdk.ui.screen.base.AbsViewModel
import com.example.samplewoundsdk.data.pojo.measurement.MeasurementMetadata
import javax.inject.Inject

class MeasurementFullScreenViewModel @Inject constructor(
) : AbsViewModel() {

    private val _boundaryListLD = MutableLiveData<ArrayList<Int>>()
    val boundaryListLD: LiveData<ArrayList<Int>>
        get() = _boundaryListLD

    fun getBoundaryLabelList(metadataList: List<MeasurementMetadata>) {
        val boundaryList = ArrayList<Int>()
        boundaryList.add(0)
        boundaryList.addAll(metadataList.mapIndexed { index, _ ->
            index + 1
        })
        _boundaryListLD.value = boundaryList
    }

}