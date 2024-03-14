package com.example.samplewoundsdk.data.pojo.measurement

import android.graphics.Point

data class IntersectPoint(val x: Double, val y: Double) {
        companion object {
            fun fromPoint(point: Point): IntersectPoint {
                return IntersectPoint(point.x.toDouble(), point.y.toDouble())
            }
        }
    }