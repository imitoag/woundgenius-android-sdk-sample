package com.example.samplewoundsdk.data.pojo.measurement

import android.graphics.Point

data class RectanglePosition(
    val outlinePointIndex: Int,
    val rectanglePointIndex: Int,
    val drawDifference: Pair<Int, Int>,
    val rectangle: ArrayList<Point>
)