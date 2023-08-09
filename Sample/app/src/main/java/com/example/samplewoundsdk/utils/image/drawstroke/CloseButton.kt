package com.example.samplewoundsdk.utils.image.drawstroke

import android.content.Context
import android.graphics.*
import com.example.samplewoundsdk.R
import kotlin.collections.ArrayList
import kotlin.math.max

class CloseButton(r: Int, context: Context) {

    private var scale = 1f
    private val whitePaint: Paint
    private val fillPaint: Paint
    private val padding: Float
    private val radiusForRect: Float

    init {
        RADIUS = r
        padding = context.resources.getDimension(R.dimen.clear_button_padding)
        radiusForRect = context.resources.getDimension(R.dimen.measurement_label_round)
        whitePaint = getWhitePaint(context)
        fillPaint = getFillPaint()
    }

    fun setscale(scale: Float) {
        this.scale = scale
    }

    var center: Point? = null

    private fun findCloseCenter(
        vertices: ArrayList<Point>,
        scalablePolylineView: StrokeScalableImageView,
        width: Int,
        height: Int,
        rectHeight: Float
    ): Point {
        val minValue = rectHeight
        var top: Point? = null
        var bottom: Point? = null
        var left: Point? = null
        var right: Point? = null
        val size: Int = vertices.size
        return if (size > 0) {

            vertices.forEach { point ->
                top = initPoint(top, point)
                bottom = initPoint(bottom, point)
                left = initPoint(left, point)
                right = initPoint(right, point)

                if (point.x > (right?.x ?: 0)) {
                    right = point
                }
                if (point.x < (left?.x ?: 0)) {
                    left = point
                }
                if (point.y < (top?.y ?: 0)) {
                    top = point
                }
                if (point.y > (bottom?.y ?: 0)) {
                    bottom = point
                }
            }

            val rightArea = width - (right?.x ?: 0)
            val bottomArea = height - (bottom?.y ?: 0)
            val leftArea = (left?.x ?: 0)
            val topArea = (top?.x ?: 0)
            val maxArea = max(max(rightArea, leftArea), max(topArea, bottomArea))

            if (maxArea < minValue) {
                Point(width / 2, height / 2)
            } else {
                val center: Point = when (maxArea) {
                    rightArea -> {
                        val point = scalablePolylineView.sourceToViewCoordInt(right ?: Point(0, 0))
                        Point(point.x + padding.toInt(), point.y)
                    }
                    leftArea -> {
                        val point = scalablePolylineView.sourceToViewCoordInt(left ?: Point(0, 0))
                        Point(point.x - padding.toInt(), point.y)
                    }
                    topArea -> {
                        val point = scalablePolylineView.sourceToViewCoordInt(top ?: Point(0, 0))
                        Point(point.x, point.y - rectHeight.toInt() - padding.toInt())
                    }
                    else -> {
                        val point = scalablePolylineView.sourceToViewCoordInt(bottom ?: Point(0, 0))
                        Point(point.x, point.y + rectHeight.toInt() + padding.toInt())
                    }
                }
                center
            }
        } else {
            Point(width / 2, height / 2)
        }
    }

    fun drawFilled(
        tempCanvas: Canvas,
        fillColor: Int,
        number: Int,
        vertices: ArrayList<Point>,
        scalablePolylineView: StrokeScalableImageView,
        mode: StrokeScalableImageView.Mode
    ): Canvas {
        val text =
            if (mode == StrokeScalableImageView.Mode.DrawSingle) "" else scalablePolylineView.context.getString(
                R.string.close_button_label,
                number
            )
        val textSize = Rect()
        whitePaint.getTextBounds(text, 0, text.length, textSize)

        var textHeight = textSize.height()
        if (textHeight == 0) {
            textHeight = (padding * 2).toInt()
        }

        center = findCloseCenter(
            vertices,
            scalablePolylineView,
            scalablePolylineView.sWidth,
            scalablePolylineView.sHeight,
            textHeight + padding * 2
        )
        center?.let { centerPoint ->
            fillPaint.color = fillColor

            val line1StartX =
                centerPoint.x.toFloat() + textSize.width() + (if (mode == StrokeScalableImageView.Mode.DrawSingle) 0f else padding)
            val line1StartY = centerPoint.y.toFloat() - textHeight
            val line1EndX = line1StartX + textHeight
            val line1EndY = centerPoint.y.toFloat()

            val line2StartX = line1StartX
            val line2StartY = line1EndY
            val line2EndX = line1EndX
            val line2EndY = line1StartY

            val rectLeft = centerPoint.x - padding
            val rectTop = line1StartY - padding
            val rectRight = line1EndX + padding
            val rectBottom = line1EndY + padding

            tempCanvas.drawRoundRect(
                rectLeft,
                rectTop,
                rectRight,
                rectBottom,
                radiusForRect,
                radiusForRect,
                fillPaint
            )
            tempCanvas.drawText(text, centerPoint.x.toFloat(), centerPoint.y.toFloat(), whitePaint)
            tempCanvas.drawLine(line1StartX, line1StartY, line1EndX, line1EndY, whitePaint)
            tempCanvas.drawLine(line2StartX, line2StartY, line2EndX, line2EndY, whitePaint)
        }
        return tempCanvas
    }

    fun drawAreaLabel(
        tempCanvas: Canvas,
        fillColor: Int,
        number: Int,
        area: Double,
        length: Double,
        vertices: ArrayList<Point>,
        scalablePolylineView: StrokeScalableImageView,
        mode: StrokeScalableImageView.Mode
    ): Canvas {
        val text = if (mode == StrokeScalableImageView.Mode.ViewStoma) {
            scalablePolylineView.context.getString(R.string.mm, String.format("%.2f", length))
        } else {
            scalablePolylineView.context.getString(
                R.string.close_button_area_label,
                number,
                String.format("%.2f", area)
            )
        }
        val textSize = Rect()
        whitePaint.getTextBounds(text, 0, text.length, textSize)

        center = findCloseCenter(
            vertices,
            scalablePolylineView,
            scalablePolylineView.sWidth,
            scalablePolylineView.sHeight,
            textSize.height() + padding * 2
        )
        center?.let { centerPoint ->
            fillPaint.color = fillColor

            val rectLeft = centerPoint.x - padding
            val rectTop = centerPoint.y - textSize.height() - padding
            val rectRight = centerPoint.x + textSize.width() + padding
            val rectBottom = centerPoint.y + padding

            tempCanvas.drawRoundRect(
                rectLeft,
                rectTop,
                rectRight,
                rectBottom,
                radiusForRect,
                radiusForRect,
                fillPaint
            )
            tempCanvas.drawText(text, centerPoint.x.toFloat(), centerPoint.y.toFloat(), whitePaint)
        }
        return tempCanvas
    }

    fun pointInsideRect(
        context: Context,
        number: Int,
        x: Float,
        y: Float,
        mode: StrokeScalableImageView.Mode
    ): Boolean {
        center?.let { centerPoint ->
            val text =
                if (mode == StrokeScalableImageView.Mode.DrawSingle) "" else context.getString(
                    R.string.close_button_label,
                    number
                )

            val textSize = Rect()
            whitePaint.getTextBounds(text, 0, text.length, textSize)

            var textHeight = textSize.height()
            if (textHeight == 0) {
                textHeight = (padding * 2).toInt()
            }

            val line1StartX =
                centerPoint.x.toFloat() + textSize.width() + (if (mode == StrokeScalableImageView.Mode.DrawSingle) 0f else padding)
            val line1StartY = centerPoint.y.toFloat() - textHeight
            val line1EndX = line1StartX + textHeight
            val line1EndY = centerPoint.y.toFloat()

            val rectLeft = centerPoint.x - padding
            val rectTop = line1StartY - padding
            val rectRight = line1EndX + padding
            val rectBottom = line1EndY + padding

            return RectF(rectLeft, rectTop, rectRight, rectBottom).contains(x, y)
        }
        return false
    }

    private fun getWhitePaint(context: Context): Paint {
        val whiteLine = Paint(Paint.ANTI_ALIAS_FLAG)
        whiteLine.style = Paint.Style.FILL
        whiteLine.color = Color.WHITE
        whiteLine.textSize = context.resources.getDimension(R.dimen.textSize14)
        whiteLine.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        whiteLine.strokeWidth = context.resources.getDimension(R.dimen.stroke_line_width)
        return whiteLine
    }

    private fun getFillPaint(): Paint {
        val whiteLine = Paint(Paint.ANTI_ALIAS_FLAG)
        whiteLine.style = Paint.Style.FILL
        return whiteLine
    }

    companion object {
        var RADIUS = 30

        private fun initPoint(edgePoint: Point?, point: Point): Point {
            var resultPoint = edgePoint
            if (resultPoint == null) {
                resultPoint = point
            }
            return resultPoint
        }

    }

}