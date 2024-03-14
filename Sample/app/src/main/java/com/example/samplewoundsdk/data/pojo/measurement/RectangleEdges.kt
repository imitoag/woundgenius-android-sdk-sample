package com.example.samplewoundsdk.data.pojo.measurement

import android.graphics.Point

data class RectangleEdges(
    val topEdge: Int,
    val bottomEdge: Int,
    val leftEdge: Int,
    val rightEdge: Int
) {
    companion object {
        fun createFromRectangle(rectangle: ArrayList<Point>): RectangleEdges {
            return RectangleEdges(rectangle[0].y, rectangle[3].y, rectangle[0].x, rectangle[1].x)
        }

        fun createFromCircle(point: Point ,radius:Float): RectangleEdges {
            val leftEdge = point.x - radius
            val rightEdge = point.x + radius
            val topEdge = point.y - radius
            val bottomEdge = point.y + radius

            return RectangleEdges(topEdge.toInt(), bottomEdge.toInt(), leftEdge.toInt(), rightEdge.toInt())
        }
    }
}
