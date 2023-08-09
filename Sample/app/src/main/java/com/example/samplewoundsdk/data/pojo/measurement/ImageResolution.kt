package com.example.samplewoundsdk.data.pojo.measurement

import com.google.gson.annotations.SerializedName
import io.fotoapparat.parameter.Resolution
import java.io.Serializable

data class ImageResolution(

    @SerializedName("width")
    val width: Int,

    @SerializedName("height")
    val height: Int

) : Serializable {

    constructor(size: Resolution) : this(size.width, size.height)

}