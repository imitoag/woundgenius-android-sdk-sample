package com.example.samplewoundsdk.data.pojo.measurement


data class LengthWidthButton(
    val outlinePointIndex: Int,
    val drawDifference: Pair<Int, Int>,
    val isAreaValid: Boolean
) {
}