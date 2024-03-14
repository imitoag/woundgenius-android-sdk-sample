package com.example.samplewoundsdk.data.pojo.measurement

import android.graphics.Point

class VerticesVector(firstPoint: Point, secondPoint: Point) {
        var x: Float
        var y: Float

        init {
            x = (secondPoint.x - firstPoint.x).toFloat()
            y = (secondPoint.y - firstPoint.y).toFloat()
        }

        /** Multiplies this vector by a scalar  */
        fun scl(scalar: Float): VerticesVector {
            x *= scalar
            y *= scalar
            return this
        }

        companion object {
            /** Calculates the 2D cross product between this and the given vector.
             * @param v2 the other vector
             * @return the cross product (Z vector)
             */
            fun crs(v1: VerticesVector, v2: VerticesVector): Float {
                return v1.x * v2.y - v1.y * v2.x
            }
        }
    }