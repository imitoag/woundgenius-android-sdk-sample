package com.example.samplewoundsdk.data.pojo.measurement

import android.graphics.Point

data class LengthWidthButtonPosition(
    val outlinePointIndex: Int,
    val drawDifference: Pair<Int, Int>,
    val point: Point
)