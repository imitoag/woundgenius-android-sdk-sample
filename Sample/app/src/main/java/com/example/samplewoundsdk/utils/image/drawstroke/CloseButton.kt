package com.example.samplewoundsdk.utils.image.drawstroke

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.max

class CloseButton(r: Int, context: Context) {

    private var scale = 1f
    private val whitePaint: Paint
    private val fillPaint: Paint
    private val padding: Float
    private val radiusForRect: Float

    init {
        RADIUS = r
        padding =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.clear_button_padding)
        radiusForRect =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.measurement_label_round)
        whitePaint = getWhitePaint(context)
        fillPaint = getFillPaint()
    }

    fun setscale(scale: Float) {
        this.scale = scale
    }

    var center: Point? = null

    private fun findBestPointToDrawCloseLabel(
        verticesList: ArrayList<com.example.samplewoundsdk.data.pojo.measurement.Vertices>,
        width: Int,
        height: Int,
        rectHeight: Float
    ): Point {
        val minValue = rectHeight
        var top: Point? = null
        var bottom: Point? = null
        var left: Point? = null
        var right: Point? = null
        val size: Int = verticesList.size
        return if (size > 0) {

            verticesList.forEach { vertices ->
                top = initPoint(top, vertices.point)
                bottom = initPoint(bottom, vertices.point)
                left = initPoint(left, vertices.point)
                right = initPoint(right, vertices.point)

                if (vertices.point.x > (right?.x ?: 0)) {
                    right = vertices.point
                }
                if (vertices.point.x < (left?.x ?: 0)) {
                    left = vertices.point
                }
                if (vertices.point.y < (top?.y ?: 0)) {
                    top = vertices.point
                }
                if (vertices.point.y > (bottom?.y ?: 0)) {
                    bottom = vertices.point
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
                        Point(right ?: Point(0, 0))
                    }

                    leftArea -> {
                        (left ?: Point(0, 0))
                    }

                    topArea -> {
                        Point(top ?: Point(0, 0))
                    }

                    else -> {
                        Point(bottom ?: Point(0, 0))
                    }
                }
                center
            }
        } else {
            Point(width / 2, height / 2)
        }
    }

    fun findBestOptionPointToDrawCloseButton(
        number: Int,
        vertices: ArrayList<com.example.samplewoundsdk.data.pojo.measurement.Vertices>,
        scalablePolylineView: StrokeScalableImageView
    ): Point {
        val text = scalablePolylineView.context.getString(
            com.example.samplewoundsdk.R.string.close_button_label,
            number
        )
        val textSize = Rect()
        whitePaint.getTextBounds(text, 0, text.length, textSize)

        var textHeight = textSize.height()
        if (textHeight == 0) {
            textHeight = (padding * 2).toInt()
        }

        return findBestPointToDrawCloseLabel(
            vertices,
            scalablePolylineView.sWidth,
            scalablePolylineView.sHeight,
            textHeight + padding * 2
        )
    }

    fun getAreaRectangleByPoint(
        centerPoint: Point, rectanglePointIndex: Int, scalablePolylineView: StrokeScalableImageView, number: Int
    ): ArrayList<Point> {
        val text = scalablePolylineView.context.getString(
            com.example.samplewoundsdk.R.string.close_button_label,
            number
        )
        val textSize = Rect()
        whitePaint.getTextBounds(text, 0, text.length, textSize)

        var textHeight = textSize.height()
        if (textHeight == 0) {
            textHeight = (padding * 2).toInt()
        }

        val rectangleHeight = padding / 2 + textHeight + padding / 2
        val rectangleWidth = padding / 2 + textSize.width() + padding / 2

        var rectLeft: Int = 0
        var rectTop: Int = 0
        var rectRight: Int = 0
        var rectBottom: Int = 0

        when (rectanglePointIndex) {
            0 -> {
                rectLeft = centerPoint.x
                rectTop = centerPoint.y
                rectRight = (centerPoint.x + rectangleWidth).toInt()
                rectBottom = (centerPoint.y + rectangleHeight).toInt()
            }

            1 -> {
                rectLeft = (centerPoint.x - rectangleWidth).toInt()
                rectTop = centerPoint.y
                rectRight = centerPoint.x
                rectBottom = (centerPoint.y + rectangleHeight).toInt()
            }

            2 -> {
                rectLeft = (centerPoint.x - rectangleWidth).toInt()
                rectTop = (centerPoint.y - rectangleHeight).toInt()
                rectRight = centerPoint.x
                rectBottom = centerPoint.y
            }

            3 -> {
                rectLeft = centerPoint.x
                rectTop = (centerPoint.y - rectangleHeight).toInt()
                rectRight = (centerPoint.x + rectangleWidth).toInt()
                rectBottom = centerPoint.y
            }

            else -> {
                rectLeft = (centerPoint.x - rectangleWidth / 2).toInt()
                rectTop = (centerPoint.y - rectangleHeight / 2).toInt()
                rectRight = (centerPoint.x + rectangleWidth / 2).toInt()
                rectBottom = (centerPoint.y + rectangleHeight / 2).toInt()
            }
        }

        val pointA = Point(
            rectLeft.toInt(),
            rectTop.toInt()
        )
        val pointB = Point(
            rectRight.toInt(),
            rectTop.toInt()
        )
        val pointC = Point(
            rectRight.toInt(),
            rectBottom.toInt()
        )
        val pointD = Point(
            rectLeft.toInt(),
            rectBottom.toInt()
        )

        val rectanglePoints: ArrayList<Point> = ArrayList()
        rectanglePoints.add(pointA)
        rectanglePoints.add(pointB)
        rectanglePoints.add(pointC)
        rectanglePoints.add(pointD)

        return rectanglePoints
    }

    fun getAreaClearRectangleByPoint(
        centerPoint: Point,
        rectanglePointIndex: Int,
        scalablePolylineView: StrokeScalableImageView,
        number: Int
    ): ArrayList<Point> {
        val text = scalablePolylineView.context.getString(
            com.example.samplewoundsdk.R.string.close_button_label,
            number
        )
        val textSize = Rect()
        whitePaint.getTextBounds(text, 0, text.length, textSize)

        var textHeight = textSize.height()
        if (textHeight == 0) {
            textHeight = (padding * 2).toInt()
        }

        val rectangleHeight = padding / 2 + textHeight + padding / 2
        val rectangleWidth =
            padding / 2 + textSize.width() + padding + textSize.height() + padding / 2

        var rectLeft: Int = 0
        var rectTop: Int = 0
        var rectRight: Int = 0
        var rectBottom: Int = 0

        when (rectanglePointIndex) {
            0 -> {
                rectLeft = centerPoint.x
                rectTop = centerPoint.y
                rectRight = (centerPoint.x + rectangleWidth).toInt()
                rectBottom = (centerPoint.y + rectangleHeight).toInt()
            }

            1 -> {
                rectLeft = (centerPoint.x - rectangleWidth).toInt()
                rectTop = centerPoint.y
                rectRight = centerPoint.x
                rectBottom = (centerPoint.y + rectangleHeight).toInt()
            }

            2 -> {
                rectLeft = (centerPoint.x - rectangleWidth).toInt()
                rectTop = (centerPoint.y - rectangleHeight).toInt()
                rectRight = centerPoint.x
                rectBottom = centerPoint.y
            }

            3 -> {
                rectLeft = centerPoint.x
                rectTop = (centerPoint.y - rectangleHeight).toInt()
                rectRight = (centerPoint.x + rectangleWidth).toInt()
                rectBottom = centerPoint.y
            }

            else -> {
                rectLeft = (centerPoint.x - rectangleWidth / 2).toInt()
                rectTop = (centerPoint.y - rectangleHeight / 2).toInt()
                rectRight = (centerPoint.x + rectangleWidth / 2).toInt()
                rectBottom = (centerPoint.y + rectangleHeight / 2).toInt()
            }
        }


        val pointA = Point(
            rectLeft,
            rectTop
        )
        val pointB = Point(
            rectRight,
            rectTop
        )
        val pointC = Point(
            rectRight,
            rectBottom
        )
        val pointD = Point(
            rectLeft,
            rectBottom
        )

        val rectanglePoints: ArrayList<Point> = ArrayList()
        rectanglePoints.add(pointA)
        rectanglePoints.add(pointB)
        rectanglePoints.add(pointC)
        rectanglePoints.add(pointD)

        return rectanglePoints
    }

    fun drawAreaClearLabelButton(
        number: Int,
        tempCanvas: Canvas,
        fillColor: Int,
        scalablePolylineView: StrokeScalableImageView,
        mode: StrokeScalableImageView.Mode,
        rectanglePoints: ArrayList<Point>
    ): Canvas {
        val text = scalablePolylineView.context.getString(
            com.example.samplewoundsdk.R.string.close_button_label,
            number
        )
        val textSize = Rect()
        whitePaint.getTextBounds(text, 0, text.length, textSize)

        var textHeight = textSize.height()
        if (textHeight == 0) {
            textHeight = (padding * 2).toInt()
        }

        if (rectanglePoints.isNotEmpty()) {

            this.center = Point(
                ((rectanglePoints[3].x + padding / 2).toInt()),
                (rectanglePoints[3].y - padding / 2).toInt()
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

                val rectLeft = rectanglePoints[0].x.toFloat()
                val rectTop = rectanglePoints[1].y.toFloat()
                val rectRight = rectanglePoints[1].x.toFloat()
                val rectBottom = rectanglePoints[2].y.toFloat()

                tempCanvas.drawRoundRect(
                    rectLeft,
                    rectTop,
                    rectRight,
                    rectBottom,
                    radiusForRect,
                    radiusForRect,
                    fillPaint
                )
                tempCanvas.drawText(
                    text,
                    centerPoint.x.toFloat(),
                    centerPoint.y.toFloat(),
                    whitePaint
                )
                tempCanvas.drawLine(line1StartX, line1StartY, line1EndX, line1EndY, whitePaint)
                tempCanvas.drawLine(line2StartX, line2StartY, line2EndX, line2EndY, whitePaint)
            }
        }
        return tempCanvas
    }

    fun drawAreaLabelButton(
        number: Int,
        tempCanvas: Canvas,
        fillColor: Int,
        scalablePolylineView: StrokeScalableImageView,
        rectanglePoints: ArrayList<Point>
    ): Canvas {
        val text = scalablePolylineView.context.getString(
            com.example.samplewoundsdk.R.string.close_button_label,
            number
        )

        val textSize = Rect()
        whitePaint.getTextBounds(text, 0, text.length, textSize)

        if (rectanglePoints.isNotEmpty()) {

            val center = Point(
                ((rectanglePoints[3].x + padding / 1.5f).toInt()),
                (rectanglePoints[3].y - padding / 2).toInt()
            )

            center.let { centerPoint ->
                fillPaint.color = fillColor

                val rectLeft = rectanglePoints[0].x.toFloat()
                val rectTop = rectanglePoints[1].y.toFloat()
                val rectRight = rectanglePoints[1].x.toFloat()
                val rectBottom = rectanglePoints[2].y.toFloat()

                tempCanvas.drawRoundRect(
                    rectLeft,
                    rectTop,
                    rectRight,
                    rectBottom,
                    radiusForRect,
                    radiusForRect,
                    fillPaint
                )
                tempCanvas.drawText(
                    text,
                    centerPoint.x.toFloat(),
                    centerPoint.y.toFloat(),
                    whitePaint
                )
            }
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
                    com.example.samplewoundsdk.R.string.close_button_label,
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

            val rectLeft = centerPoint.x - (padding / 2)
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
        whiteLine.textSize =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.textSize14)
        whiteLine.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        whiteLine.strokeWidth =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.stroke_line_width)
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