package com.example.samplewoundsdk.data.pojo.measurement

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MeasurementMetadata(

    val area: Double? = null,

    @SerializedName("circumference")
    val circumference: Double? = null,

    @SerializedName("length")
    var length: Double?,

    @SerializedName("width")
    var width: Double? = null,

    @SerializedName("depth")
    var depth: Double? = null,

    @SerializedName("vertices")
    val vertices: List<Point>? = null,

    @SerializedName("length_line")
    val lengthLine: Line? = null,

    @SerializedName("width_line")
    val widthLine: Line? = null,

    @SerializedName("count_px_in_cm")
    val countPxInCm: Int

) : Serializable {

    data class Point(

        @SerializedName("x")
        val x: Int,

        @SerializedName("y")
        val y: Int

    ) : Serializable {

        fun scale(scale: Double) = Point((x * scale).toInt(), (y * scale).toInt())

    }

    data class Line(

        @SerializedName("point_a_index")
        val pointAIndex: Int,

        @SerializedName("point_b_index")
        val pointBIndex: Int

    ) : Serializable


}