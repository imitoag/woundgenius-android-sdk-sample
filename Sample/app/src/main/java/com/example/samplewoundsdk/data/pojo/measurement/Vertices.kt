package com.example.samplewoundsdk.data.pojo.measurement

import android.graphics.Point
import android.graphics.PointF
import com.google.gson.annotations.SerializedName
import io.fotoapparat.parameter.Resolution
import java.io.Serializable

data class Vertices(

    @SerializedName("point")
    var point:Point,

    @SerializedName("isEnabled")
    var isEnabled: Boolean = false

) : Serializable {
    constructor(pointF: PointF) : this(Point(pointF.x.toInt(), pointF.y.toInt()))
}