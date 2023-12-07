package com.example.samplewoundsdk.utils.image.drawstroke

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.text.TextPaint
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.measurement.Vertices
import kotlin.math.abs

class StrokeScalableImageView : SubsamplingScaleImageView {

    private var closeButtonRadius = 0f
    var textSize = 0f
    private var lineWidth = 0f
    var minDistance = 0f
    private var greenColor = 0
    private var transparentGreenColor = 0
    private val polygonPath = Path()
    private val fingerLinePath = Path()
    var verticesList = ArrayList<ArrayList<Vertices>>()
    private var visibilityVerticesIndexes = ArrayList(verticesList.mapIndexed { index, _ -> index })
    private var vertexPaint: Paint? = null
    private var vertexDisabledPaint: Paint? = null
    private var vertexStrokePaint: Paint? = null
    private var pathPaint: Paint? = null
    private var linesPaint: Paint? = null
    private var textPaint: Paint? = null
    private var linesStrokePaint: Paint? = null
    private var currentPosition: PointF = PointF()
    private var firstPoint = Point()
    private var isPathClosed = ArrayList<Boolean>()
    private var currentDraggedVertices: Vertices? = null
    private var fillPathPaint: Paint? = null
    private var clearPaint: Paint? = null
    private var isClear = false
    private var closeButton = ArrayList<CloseButton>()
    private var isTouchUP = false
    private var isNeedWhiteStroke = false
    private var zoom = 0f
    private var touchListener: ViewTouchListener? = null
    private var widthIndexes: ArrayList<Pair<Int?, Int?>>? = null
    private var lengthIndexes: ArrayList<Pair<Int?, Int?>>? = null
    private var areaList = ArrayList<Double>()
    private var mode = Mode.Draw
    private var circlePaint: Paint? = null
    private var isLinesDrawing = false
    private var touchEventHandle = Handler()
    private var zoomChangedTime = 0L
    private var showOutlines = true

    private var diameter = 0.0

    fun setMode(mode: Mode) {
        this.mode = mode
    }

    fun setDiameter(diameter: Double) {
        this.diameter = diameter
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attr: AttributeSet?) : super(context, attr) {
        init()
    }

    private fun init() {
        closeButtonRadius = context.resources.getDimension(R.dimen.stroke_circle_radius)
        textSize = context.resources.getDimension(R.dimen.stroke_text_size)
        lineWidth = context.resources.getDimension(R.dimen.stroke_line_width)
        minDistance = context.resources.getDimension(R.dimen.stroke_min_distance)
        greenColor = ContextCompat.getColor(context, R.color.sample_app_color_green)
        transparentGreenColor = ContextCompat.getColor(context, R.color.sample_app_color_green_transparent)
        initPaints()
        buildDrawingCache()
        setDoubleTapZoomScale(3f)
    }

    private fun initPaints() {
        RADIUS = (lineWidth * 2)
        vertexPaint = initVertexPaint()
        vertexDisabledPaint = initVertexDisabledPaint()
        vertexStrokePaint = initVertexStrokePaint()
        pathPaint = initPathPaint()
        fillPathPaint = initFillPathPaint()
        clearPaint = initClearPaint()
        closeButton.add(CloseButton(closeButtonRadius.toInt(), context))
        textPaint = TextPaint()
        (textPaint as TextPaint).color = greenColor
        (textPaint as TextPaint).textSize = textSize
        circlePaint = Paint()
        circlePaint!!.color = Color.WHITE
        circlePaint!!.style = Paint.Style.FILL_AND_STROKE
        linesPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linesPaint!!.color = Color.GREEN
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint!!.color = Color.WHITE
        textPaint!!.textSize = 26f
        textPaint!!.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        linesStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linesStrokePaint!!.color = resources.getColor(R.color.sample_app_color_white_opacity_80)
        linesStrokePaint!!.strokeWidth = lineWidth * 2
        linesStrokePaint!!.style = Paint.Style.STROKE
    }

    private fun initClearPaint(): Paint {
        val clearPaint = Paint()
        clearPaint.style = Paint.Style.FILL
        clearPaint.alpha = 0xFF
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        return clearPaint
    }

    private fun initVertexStrokePaint(): Paint {
        val vertexStrokePaint = Paint()
        vertexStrokePaint.isAntiAlias = true
        vertexStrokePaint.color = Color.WHITE
        vertexStrokePaint.style = Paint.Style.STROKE
        vertexStrokePaint.strokeWidth = lineWidth
        return vertexStrokePaint
    }

    private fun initPathPaint(): Paint {
        val pathPaint = Paint()
        pathPaint.isAntiAlias = true
        pathPaint.color = greenColor
        pathPaint.strokeWidth = lineWidth
        pathPaint.style = Paint.Style.STROKE
        return pathPaint
    }

    private fun initFillPathPaint(): Paint {
        val pathPaint = Paint()
        pathPaint.isAntiAlias = true
        vertexPaint!!.strokeWidth = 5f
        pathPaint.color = transparentGreenColor
        pathPaint.style = Paint.Style.FILL
        return pathPaint
    }

    private fun initVertexPaint(): Paint {
        val vertexPaint = Paint()
        vertexPaint.isAntiAlias = true
        vertexPaint.color = greenColor
        vertexPaint.strokeWidth = lineWidth
        return vertexPaint
    }

    private fun initVertexDisabledPaint(): Paint {
        val vertexPaint = Paint()
        vertexPaint.isAntiAlias = true
        vertexPaint.color = greenColor
        vertexPaint.strokeWidth = lineWidth
        return vertexPaint
    }

    fun setTouchListener(touchListener: ViewTouchListener?) {
        this.touchListener = touchListener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (System.currentTimeMillis() - zoomChangedTime < 500) return super.onTouchEvent(event)
        if (event.pointerCount > 1 || (mode != Mode.Draw && mode != Mode.DrawSingle) || !showOutlines) {
            touchEventHandle.removeCallbacksAndMessages(null)
            if (verticesList.lastOrNull()?.size == 1) {
                verticesList.lastOrNull()?.clear()
            }
            currentDraggedVertices = null
            return super.onTouchEvent(event)
        }

        if (System.currentTimeMillis() - event.downTime < 150) {
            return true
        }
        if (mode != Mode.Draw && mode != Mode.DrawSingle) return true

        val action = event.action
        var x = event.x
        var y = event.y
        val originalX = event.x
        val originalY = event.y
        val pointF = viewToSourceCoord(x, y)
        if (pointF != null) {
            x = pointF.x
            y = pointF.y
        }

        touchEventHandle.postDelayed({
            if (event.pointerCount == 1) {
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        touchStart(x, y, originalX, originalY)
                        if (touchListener != null && pointF != null) touchListener!!.onDown(
                            pointF
                        )
                        if (touchListener != null) touchListener!!.onVertexListChanged(
                            verticesList,
                            isAllPathClosed()
                        )
                        postInvalidate()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        touchMove(x, y)
                        if (touchListener != null && pointF != null) touchListener!!.onMove(
                            pointF
                        )
                        if (touchListener != null) touchListener!!.onVertexListChanged(
                            verticesList,
                            isAllPathClosed()
                        )
                        postInvalidate()
                    }

                    MotionEvent.ACTION_UP -> {
                        touchUp(x, y)
                        if (touchListener != null) touchListener!!.onUp()
                        postInvalidate()
                    }
                }
            }
        }, 100)
        return true
    }

    private fun adjustVerticesDistance(
        firstAdjustedVertices: List<Vertices>,
        secondAdjustedVertices: List<Vertices>
    ): Pair<List<Vertices>, List<Vertices>> {

        val newPointsForFirstAdjustedList = ArrayList<Vertices>()
        val newPointsForSecondAdjustedList = ArrayList<Vertices>()

        var compareVertices = firstAdjustedVertices[0]
        newPointsForFirstAdjustedList.add(firstAdjustedVertices[0])
        for (i in 1..firstAdjustedVertices.lastIndex) {
            val vertix = firstAdjustedVertices[i]
            val distance = PolygonGeometry.calculateDistance(
                compareVertices.point,
                vertix.point
            )
            if (distance.toInt() >= POINTS_RANGE_PX * 2) {
                val newPoint = addAdditionalDotsBetweenAdjustedPoints(
                    compareVertices.point,
                    vertix.point
                )
                newPoint?.let {
                    newPointsForFirstAdjustedList.add(
                        Vertices(
                            newPoint
                        )
                    )
                }
            }
            newPointsForFirstAdjustedList.add(vertix)
            compareVertices = vertix
        }

        if (secondAdjustedVertices.isNotEmpty()) {
            val distance = PolygonGeometry.calculateDistance(
                firstAdjustedVertices.last().point,
                secondAdjustedVertices.first().point
            )
            if (distance.toInt() >= POINTS_RANGE_PX * 2) {
                val newPoint = addAdditionalDotsBetweenAdjustedPoints(
                    firstAdjustedVertices.last().point,
                    secondAdjustedVertices.first().point
                )
                newPoint?.let {
                    newPointsForFirstAdjustedList.add(
                        Vertices(
                            newPoint
                        )
                    )
                }
            }

            compareVertices = secondAdjustedVertices[0]
            newPointsForSecondAdjustedList.add(secondAdjustedVertices[0])
            for (i in 1..secondAdjustedVertices.lastIndex) {
                val vertix = secondAdjustedVertices[i]
                val distance = PolygonGeometry.calculateDistance(
                    compareVertices.point,
                    vertix.point
                )
                if (distance.toInt() >= POINTS_RANGE_PX * 2) {
                    val newPoint = addAdditionalDotsBetweenAdjustedPoints(
                        compareVertices.point,
                        vertix.point
                    )
                    newPoint?.let {
                        newPointsForSecondAdjustedList.add(
                            Vertices(
                                newPoint
                            )
                        )
                    }
                }
                newPointsForSecondAdjustedList.add(vertix)
                compareVertices = vertix
            }
        }

        return Pair(newPointsForFirstAdjustedList, newPointsForSecondAdjustedList)
    }

    private fun addAdditionalDotsBetweenAdjustedPoints(
        point: Point,
        finalePoint: Point
    ): Point? {
        val newPointX = (point.x + finalePoint.x) / 2
        val newPointY = (point.y + finalePoint.y) / 2

        val resultPoint = Point(newPointX, newPointY)
        val resultDistance = PolygonGeometry.calculateDistance(resultPoint, finalePoint)

        return if (resultDistance >= POINTS_RANGE_PX && resultPoint != finalePoint) {
            resultPoint
        } else {
            null
        }
    }


    private fun isAllPathClosed(): Boolean {
        var isAllPathClosed = true
        isPathClosed.forEachIndexed { index, b ->
            if (!(index == isPathClosed.lastIndex && verticesList.lastOrNull().isNullOrEmpty())) {
                if (!b) {
                    isAllPathClosed = false
                }
            }
        }
        return if (verticesList.isEmpty()) {
            false
        } else {
            if (verticesList.size == 1) {
                if (isPathClosed.isEmpty()) {
                    false
                } else isPathClosed.last()
            } else {
                isAllPathClosed
            }
        }
    }

    private fun isPointCanBeMoved(): Boolean {
        val isPointsCanBeMoved =
            if (isPathClosed.size > 0) {
                if (verticesList.lastOrNull().isNullOrEmpty()) {
                    true
                } else {
                    isPathClosed.find { !it } == null
                }
            } else {
                true
            }
        return isPointsCanBeMoved
    }


    private fun touchStart(eventX: Float, eventY: Float, originalX: Float, originalY: Float) {
        isTouchUP = false
        if (isPointCanBeMoved()) {
            currentDraggedVertices = findNearestPoint(eventX, eventY)
        }
        closeButton.forEachIndexed { index, closeButton ->
            if (closeButton.center != null) {
                if (processClearClick(originalX, originalY, closeButton, index)) {
                    return
                }
            }
        }
        if (currentDraggedVertices == null && !isClear) {
            if (verticesList.size > 1 && mode == Mode.DrawSingle) {
                return
            }
            if (verticesList.size == 0) {
                verticesList = ArrayList<ArrayList<Vertices>>().apply {
                    add(ArrayList())
                }
                verticesList.lastOrNull()?.add(
                    Vertices(
                        Point(eventX.toInt(), eventY.toInt())
                    )
                )
                isPathClosed.add(false)
            } else {
                if (verticesList.lastOrNull()?.isEmpty() == true) {
                    verticesList.lastOrNull()?.add(
                        Vertices(
                            Point(eventX.toInt(), eventY.toInt())
                        )
                    )
                    isPathClosed[isPathClosed.lastIndex] = false
                } else {
                    if (isPathClosed.isNotEmpty() && verticesList.isNotEmpty() && verticesList.lastOrNull()
                            ?.isNotEmpty() == true
                    ) {
                        if (!isPathClosed[isPathClosed.lastIndex] && !isLinesDrawing) {
                            var x = eventX
                            var y = eventY
                            var p: PointF? = PointF(x, y)
                            p = sourceToViewCoord(p)
                            if (p != null) {
                                x = p.x
                                y = p.y
                                val receivedPoint = viewToSourceCoord(PointF(x, y)) ?: PointF()
                                val verticesLastPoint = verticesList.lastOrNull()?.lastIndex?.let {
                                    verticesList.lastOrNull()?.get(
                                        it
                                    )
                                }
                                val lastDrawnPoint = verticesLastPoint?.point?.let {
                                    PointF(
                                        it.x.toFloat(),
                                        it.y.toFloat()
                                    )
                                } ?: PointF()
                                val isPointsIdentical =
                                    receivedPoint.x.toInt() == lastDrawnPoint.x.toInt() && receivedPoint.y.toInt() == lastDrawnPoint.y.toInt()

                                if ((verticesList.lastOrNull()?.size
                                        ?: 0) > 0 && !isPointsIdentical
                                ) {
                                    isLinesDrawing = true
                                    currentPosition = receivedPoint

                                    addAdditionalDotsBetweenReceivedViewPoint(receivedPoint)

                                    isLinesDrawing = false
                                    firstPoint =
                                        verticesList.lastOrNull()?.get(0)?.point ?: Point(0, 0)

                                    p = viewToSourceCoord(p)
                                    x = p!!.x
                                    y = p.y
                                    val isXaround =
                                        x < firstPoint.x + POINTS_RANGE_PX && x > firstPoint.x - POINTS_RANGE_PX
                                    val isYaround =
                                        y < firstPoint.y + POINTS_RANGE_PX && y > firstPoint.y - POINTS_RANGE_PX
                                    if (isXaround && isYaround && (verticesList.lastOrNull()?.size
                                            ?: 0) > 2
                                    ) {
                                        val firstPoint = verticesList.lastOrNull()?.first()?.point
                                        val lastPoint = verticesList.lastOrNull()?.last()?.point
                                        val distance =
                                            PolygonGeometry.calculateDistance(lastPoint, firstPoint)
                                        if (distance < POINTS_RANGE_PX) {
                                            verticesList.lastOrNull()?.dropLast(1)
                                        }
                                        fingerLinePath.reset()
                                        isPathClosed[isPathClosed.lastIndex] = true
                                        validateAvailablePoints()
                                    }
                                }
                            }
                        }
                    } else {
                        if (verticesList.isNotEmpty() && verticesList.lastOrNull()
                                ?.isEmpty() == true
                        ) {
                            verticesList.lastOrNull()
                                ?.add(
                                    Vertices(
                                        Point(
                                            eventX.toInt(),
                                            eventY.toInt()
                                        )
                                    )
                                )
                        }
                    }
                }
            }
        } else {
            movePointsToSameDirection(eventX, eventY)
        }
    }

    private fun checkIfDistanceBetweenDots(receivedPoint: PointF): Boolean {
        verticesList.lastOrNull()?.lastIndex?.let {
            val lastPoint = verticesList.lastOrNull()?.get(it)
            val lastDrawnPoint =
                lastPoint?.point?.let { PointF(it.x.toFloat(), it.y.toFloat()) } ?: PointF()
            val distance = PolygonGeometry.calculateDistance(
                lastDrawnPoint,
                receivedPoint
            )
            return distance >= POINTS_RANGE_PX
        } ?: return false
    }

    private fun checkIfDistanceBetweenAutoDetectionDots(
        currentPoint: PointF,
        finalPoint: PointF
    ): Boolean {
        val distance = PolygonGeometry.calculateDistance(
            currentPoint,
            finalPoint
        )
        return distance >= POINTS_RANGE_PX
    }

    private fun touchUp(eventX: Float, eventY: Float) {
        isTouchUP = true
        if (isPathClosed.isNotEmpty()) {
            if (!isPathClosed.last() && currentDraggedVertices == null && !isClear && (verticesList.lastOrNull()?.size
                    ?: 0) > 0
            ) {
                if (!(verticesList.size > 1 && mode == Mode.DrawSingle)) {
                    if (isPathClosed.isNotEmpty() && verticesList.isNotEmpty()) {
                        if (!isPathClosed[isPathClosed.lastIndex] && !isLinesDrawing) {
                            var x = eventX
                            var y = eventY
                            var p: PointF? = PointF(x, y)
                            p = sourceToViewCoord(p)
                            if (p != null) {
                                x = p.x
                                y = p.y
                                val receivedPoint = viewToSourceCoord(PointF(x, y)) ?: PointF()
                                val verticesLastPoint = verticesList.lastOrNull()?.lastIndex?.let {
                                    verticesList.lastOrNull()?.get(
                                        it
                                    )
                                }
                                val lastDrawnPoint =
                                    verticesLastPoint?.point?.let {
                                        PointF(it.x.toFloat(), it.y.toFloat())
                                    } ?: PointF()
                                val isPointsIdentical =
                                    receivedPoint.x.toInt() == lastDrawnPoint.x.toInt() && receivedPoint.y.toInt() == lastDrawnPoint.y.toInt()

                                if ((verticesList.lastOrNull()?.size
                                        ?: 0) > 0 && !isPointsIdentical
                                ) {
                                    isLinesDrawing = true
                                    currentPosition = receivedPoint

                                    addAdditionalDotsBetweenReceivedViewPoint(receivedPoint)

                                    isLinesDrawing = false
                                    firstPoint =
                                        verticesList.lastOrNull()?.get(0)?.point ?: Point(0, 0)
                                    p = viewToSourceCoord(p)
                                    x = p!!.x
                                    y = p.y
                                    val isXaround =
                                        x < firstPoint.x + POINTS_RANGE_PX && x > firstPoint.x - POINTS_RANGE_PX
                                    val isYaround =
                                        y < firstPoint.y + POINTS_RANGE_PX && y > firstPoint.y - POINTS_RANGE_PX
                                    if (isXaround && isYaround && (verticesList.lastOrNull()?.size
                                            ?: 0) > 2
                                    ) {
                                        val firstPoint = verticesList.lastOrNull()?.first()?.point
                                        val lastPoint = verticesList.lastOrNull()?.last()?.point
                                        val distance =
                                            PolygonGeometry.calculateDistance(lastPoint, firstPoint)
                                        if (distance < POINTS_RANGE_PX) {
                                            verticesList.lastOrNull()?.dropLast(1)
                                        }
                                        fingerLinePath.reset()
                                        isPathClosed[isPathClosed.lastIndex] = true
                                        validateAvailablePoints()
                                    }
                                }
                            }
                        }
                    } else {
                        isPathClosed.add(false)
                        verticesList.lastOrNull()
                            ?.add(
                                Vertices(
                                    Point(
                                        eventX.toInt(),
                                        eventY.toInt()
                                    )
                                )
                            )
                    }
                }
            }
        }

        if (isPointCanBeMoved() && currentDraggedVertices != null) {
            movePointsToSameDirection(eventX, eventY)

        } else if (currentDraggedVertices != null) drawPath(eventX, eventY)
    }

    private fun addAdditionalDotsBetweenReceivedViewPoint(receivedPoint: PointF) {
        do {
            val lastPoint = verticesList.lastOrNull()?.lastIndex?.let {
                verticesList.lastOrNull()?.get(it)?.point
            }?.let { PointF(it.x.toFloat(), it.y.toFloat()) } ?: PointF()

            val isXvalueLower = (lastPoint.x - currentPosition.x) >= POINTS_RANGE_PX
            val isXvalueBigger = (lastPoint.x + POINTS_RANGE_PX) <= currentPosition.x
            val isYvalueLower = (lastPoint.y - currentPosition.y) >= POINTS_RANGE_PX
            val isYvalueBigger = (lastPoint.y + POINTS_RANGE_PX) <= currentPosition.y

            val currentX = if (isXvalueLower) {
                lastPoint.x - POINTS_RANGE_PX
            } else if (isXvalueBigger) {
                lastPoint.x + POINTS_RANGE_PX
            } else {
                if (lastPoint.x != currentPosition.x) {
                    if (lastPoint.x - currentPosition.x < 0) {
                        lastPoint.x + (((lastPoint.x - currentPosition.x) * -1))
                    } else {
                        lastPoint.x - (lastPoint.x - currentPosition.x)
                    }
                } else {
                    currentPosition.x
                }
            }.toFloat()

            val currentY = if (isYvalueLower) {
                lastPoint.y - POINTS_RANGE_PX
            } else if (isYvalueBigger) {
                lastPoint.y + POINTS_RANGE_PX
            } else {
                if (lastPoint.y != currentPosition.y) {
                    if (lastPoint.y - currentPosition.y < 0) {
                        lastPoint.y + (((lastPoint.y - currentPosition.y) * -1))
                    } else {
                        lastPoint.y - (lastPoint.y - currentPosition.y)
                    }
                } else {
                    currentPosition.y
                }
            }.toFloat()


            val point = Point(currentX.toInt(), currentY.toInt())
            val lastVertices = verticesList.lastOrNull()?.lastOrNull()?.point
            val resultDistance = PolygonGeometry.calculateDistance(lastVertices, point)

            if (verticesList.lastOrNull() != point && resultDistance >= POINTS_RANGE_PX) {
                verticesList.lastOrNull()?.add(
                    Vertices(
                        point
                    )
                )
                fingerLinePath.reset()
                fingerLinePath.moveTo(
                    point.x.toFloat(),
                    point.y.toFloat()
                ) //!!!
                fingerLinePath.lineTo(
                    point.x.toFloat(),
                    point.y.toFloat()
                )
            }
        } while (checkIfDistanceBetweenDots(receivedPoint))
    }

    private fun addAdditionalDotsBetweenAutoDetectionPoints(
        point: PointF,
        finalePoint: PointF
    ): ArrayList<PointF> {
        val resultPointList = ArrayList<PointF>()
        resultPointList.add(point)
        do {
            val lastPoint = resultPointList.last()
            val isXvalueLower = (lastPoint.x - finalePoint.x) >= POINTS_RANGE_PX
            val isXvalueBigger = (lastPoint.x + POINTS_RANGE_PX) <= finalePoint.x
            val isYvalueLower = (lastPoint.y - finalePoint.y) >= POINTS_RANGE_PX
            val isYvalueBigger = (lastPoint.y + POINTS_RANGE_PX) <= finalePoint.y

            val currentX = if (isXvalueLower) {
                lastPoint.x - POINTS_RANGE_PX
            } else if (isXvalueBigger) {
                lastPoint.x + POINTS_RANGE_PX
            } else {
                if (lastPoint.x != finalePoint.x) {
                    if (lastPoint.x - finalePoint.x < 0) {
                        lastPoint.x + (((lastPoint.x - finalePoint.x) * -1))
                    } else {
                        lastPoint.x - (lastPoint.x - finalePoint.x)
                    }
                } else {
                    finalePoint.x
                }
            }.toFloat()

            val currentY = if (isYvalueLower) {
                lastPoint.y - POINTS_RANGE_PX
            } else if (isYvalueBigger) {
                lastPoint.y + POINTS_RANGE_PX
            } else {
                if (lastPoint.y != finalePoint.y) {
                    if (lastPoint.y - finalePoint.y < 0) {
                        lastPoint.y + (((lastPoint.y - finalePoint.y) * -1))
                    } else {
                        lastPoint.y - (lastPoint.y - finalePoint.y)
                    }
                } else {
                    finalePoint.y
                }
            }.toFloat()


            val resultPoint = PointF(currentX, currentY)
            val resultDistance = PolygonGeometry.calculateDistance(resultPoint, finalePoint)

            if (resultDistance >= POINTS_RANGE_PX && resultPoint != finalePoint) {
                resultPointList.add(resultPoint)
            }
        } while (checkIfDistanceBetweenAutoDetectionDots(resultPoint, finalePoint))
        resultPointList.add(finalePoint)
        return resultPointList
    }

    private fun touchMove(x: Float, y: Float) {
        if (isPointCanBeMoved() && currentDraggedVertices != null) {
            movePointsToSameDirection(x, y)

        } else {
            drawPath(x, y)
        }
    }

    private fun getLeftIndex(index: Int, lastIndex: Int): Int {
        return when (index) {
            0 -> {
                lastIndex
            }

            else -> {
                index - 1

            }
        }
    }

    private fun getRightIndex(index: Int, lastIndex: Int): Int {
        return when (index) {
            lastIndex -> {
                0
            }

            else -> {
                index + 1
            }
        }
    }

    private fun movePointsToSameDirection(x: Float, y: Float) {
        currentDraggedVertices?.let { draggedVertices ->

            val imagePoint = Point(x.toInt(), y.toInt())

            val distanceX = imagePoint.x - draggedVertices.point.x
            val distanceY = imagePoint.y - draggedVertices.point.y

            currentDraggedVertices!!.point.x = imagePoint.x
            currentDraggedVertices!!.point.y = imagePoint.y

            var newPoints = Pair<List<Vertices>, List<Vertices>>(emptyList(), emptyList())
            var adjustedVertices: ArrayList<Vertices>? = null

            var startIndexOfFirstList = -1
            var endIndexOfFirstList = -1

            var startIndexOfSecondList = -1
            var endIndexOfSecondList = -1

            verticesList.forEach { vertices ->
                vertices.forEachIndexed { index, vertix ->
                    if (vertix.isEnabled) {
                        if (vertix.point == draggedVertices.point) {

                            val adjustPointsIndexes = ArrayList<Int>()

                            adjustPointsIndexes.add(index)

                            val firstPointIndex = getLeftIndex(index, vertices.lastIndex)
                            val secondPointIndex = getRightIndex(index, vertices.lastIndex)

                            if (vertices.find { !it.isEnabled } != null) {
                                adjustPointsIndexes.add(0, firstPointIndex)
                                adjustPointsIndexes.add(secondPointIndex)

                                if (vertices.size >= 5) {
                                    val thirdPointIndex =
                                        getLeftIndex(firstPointIndex, vertices.lastIndex)
                                    val fourthPointIndex =
                                        getRightIndex(secondPointIndex, vertices.lastIndex)
                                    adjustPointsIndexes.add(0, thirdPointIndex)
                                    adjustPointsIndexes.add(fourthPointIndex)
                                }

                                val middleAdjustedIndex =
                                    adjustPointsIndexes[adjustPointsIndexes.size / 2]
                                adjustPointsIndexes.forEach { index ->

                                    val multiplier = if (abs(middleAdjustedIndex - index) == 1) {
                                        FIRST_TWO_POINTS_MULTIPLIER
                                    } else if (abs(middleAdjustedIndex - index) == 2) {
                                        SECOND_TWO_POINTS_MULTIPLIER
                                    } else {
                                        1.0
                                    }

                                    if (multiplier != 1.0) {
                                        vertices[index].point.x += (distanceX * multiplier).toInt()
                                        vertices[index].point.y += (distanceY * multiplier).toInt()
                                    }
                                }
                            }


                            val startIndex =
                                getLeftIndex(adjustPointsIndexes.first(), vertices.lastIndex)
                            val endIndex =
                                getRightIndex(adjustPointsIndexes.last(), vertices.lastIndex)


                            val firstIndexesList: List<Vertices>
                            val secondIndexesList: List<Vertices>

                            if (endIndex < startIndex) {
                                startIndexOfFirstList = startIndex
                                endIndexOfFirstList = vertices.size
                                firstIndexesList =
                                    vertices.subList(startIndexOfFirstList, endIndexOfFirstList)

                                startIndexOfSecondList = 0
                                endIndexOfSecondList = endIndex + 1

                                secondIndexesList =
                                    vertices.subList(startIndexOfSecondList, endIndexOfSecondList)
                            } else {
                                startIndexOfFirstList = startIndex
                                endIndexOfFirstList = endIndex + 1
                                firstIndexesList =
                                    vertices.subList(startIndexOfFirstList, endIndexOfFirstList)
                                secondIndexesList = emptyList()
                            }

                            newPoints = adjustVerticesDistance(firstIndexesList, secondIndexesList)
                            adjustedVertices = vertices
                        }
                    }
                }
            }
            adjustedVertices?.let {
                val adjustedVerticesIndex = verticesList.indexOf(adjustedVertices)
                if (isPathClosed[adjustedVerticesIndex] && adjustedVertices?.find { it.point == currentDraggedVertices?.point } != null) {
                    verticesList[adjustedVerticesIndex].subList(
                        startIndexOfFirstList,
                        endIndexOfFirstList
                    ).clear()
                    verticesList[adjustedVerticesIndex].addAll(
                        startIndexOfFirstList,
                        newPoints.first
                    )
                    if (newPoints.second.isNotEmpty()) {
                        verticesList[adjustedVerticesIndex].subList(
                            startIndexOfSecondList,
                            endIndexOfSecondList
                        ).clear()
                        verticesList[adjustedVerticesIndex].addAll(
                            startIndexOfSecondList,
                            newPoints.second
                        )
                        true
                    }
                }

                val currentDraggedIndex =
                    verticesList[adjustedVerticesIndex].indexOfFirst { it == currentDraggedVertices }
                adjustCloseDistanceVertices(adjustedVerticesIndex, currentDraggedIndex)
            }
        }
    }

    private fun adjustCloseDistanceVertices(
        adjustedVertices: Int,
        currentDraggedIndex: Int
    ) {
        if (adjustedVertices >= 0) {
            val removePointIndexes = ArrayList<Int>()
            if (verticesList[adjustedVertices].isNotEmpty() && isPathClosed[adjustedVertices] && verticesList[adjustedVertices].find { it.point == currentDraggedVertices?.point } != null) {
                var compareVertices = verticesList[adjustedVertices].first()
                verticesList[adjustedVertices].forEachIndexed { index, vertix ->
                    if (compareVertices != vertix) {
                        val distance = PolygonGeometry.calculateDistance(
                            compareVertices.point,
                            vertix.point
                        )
                        if (distance.toInt() < POINTS_RANGE_PX) {
                            if (index == currentDraggedIndex) {
                                val previousIndex =
                                    verticesList[adjustedVertices].indexOf(compareVertices)
                                removePointIndexes.add(previousIndex)
                            } else {
                                removePointIndexes.add(index)
                            }
                        }
                    }
                    compareVertices = vertix
                }

                val distance = PolygonGeometry.calculateDistance(
                    verticesList[adjustedVertices].first().point,
                    verticesList[adjustedVertices].last().point
                )
                if (distance.toInt() < POINTS_RANGE_PX) {
                    if (0 == currentDraggedIndex) {
                        removePointIndexes.add(verticesList[adjustedVertices].lastIndex)
                    } else {
                        removePointIndexes.add(0)
                    }
                }
            }
            if (verticesList[adjustedVertices].filter { it.isEnabled }.size > 3) {
                removePointIndexes.distinct().sortedByDescending { it }.forEach {
                    if (it != currentDraggedIndex) {
                        verticesList[adjustedVertices].removeAt(it)
                    }
                }
            }
        }
    }

    private fun findNearestPoint(x: Float, y: Float): Vertices? {
        val distances = SparseArray<Vertices?>()
        val position = Point(x.toInt(), y.toInt())
        val nearestPoint: Vertices?

        val allowedDistance = this.context.toPx(40)
        verticesList.forEach { vertices ->
            vertices.filter { it.isEnabled }.forEach { vertix ->
                val compareVerticesInViewCoordinate = sourceToViewCoordInt(position)
                val vertixInViewCoordinate = sourceToViewCoordInt(vertix.point)

                val calculateDistance =
                    PolygonGeometry.calculateDistance(
                        compareVerticesInViewCoordinate,
                        vertixInViewCoordinate
                    ).toInt()

                if (calculateDistance < allowedDistance) {
                    distances.put(calculateDistance, vertix)
                }
            }
        }
        if (distances.size() == 0) return null
        val keyAt = distances.keyAt(0)
        nearestPoint = distances[keyAt, null]
        return nearestPoint
    }

    private fun Context.toPx(dp: Int): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    )


    private fun drawPath(eventX: Float, eventY: Float) {
        try {
            var x = eventX
            var y = eventY
            var p: PointF? = PointF(x, y)
            p = sourceToViewCoord(p)
            if (p != null) {
                x = p.x
                y = p.y

                val receivedPoint = viewToSourceCoord(PointF(x, y)) ?: PointF()
                val isPointsIdentical =
                    currentPosition.x.toInt() == receivedPoint.x.toInt() && currentPosition.y.toInt() == receivedPoint.y.toInt()

                if ((verticesList.lastOrNull()?.size ?: 0) > 0 && !isPointsIdentical) {
                    currentPosition = receivedPoint
                    val verticesLastPoint = verticesList.lastOrNull()?.lastIndex?.let {
                        verticesList.lastOrNull()?.get(
                            it
                        )
                    }
                    val lastPoint =
                        verticesLastPoint?.point?.let { PointF(it.x.toFloat(), it.y.toFloat()) }
                            ?: PointF()

                    val distance = PolygonGeometry.calculateDistance(lastPoint, currentPosition)
                    if (distance.toInt() > POINTS_RANGE_PX) {

                        val steps = (distance / POINTS_RANGE_PX).toInt()
                        var addDistance = POINTS_RANGE_PX
                        for (i in 1..steps step 1) {
                            val isXvalueLower = (lastPoint.x - currentPosition.x) >= POINTS_RANGE_PX
                            val isXvalueBigger =
                                (lastPoint.x + POINTS_RANGE_PX) <= currentPosition.x

                            val isYvalueLower = (lastPoint.y - currentPosition.y) >= POINTS_RANGE_PX
                            val isYvalueBigger =
                                (lastPoint.y + POINTS_RANGE_PX) <= currentPosition.y

                            val currentX = if (isXvalueLower) {
                                lastPoint.x - addDistance
                            } else if (isXvalueBigger) {
                                lastPoint.x + addDistance
                            } else {
                                if (lastPoint.x != currentPosition.x) {
                                    if (lastPoint.x - currentPosition.x < 0) {
                                        lastPoint.x + (((lastPoint.x - currentPosition.x) * -1) / steps)
                                    } else {
                                        lastPoint.x - (lastPoint.x - currentPosition.x) / steps
                                    }
                                } else {
                                    currentPosition.x
                                }
                            }.toFloat()


                            val currentY = if (isYvalueLower) {
                                lastPoint.y - addDistance
                            } else if (isYvalueBigger) {
                                lastPoint.y + addDistance
                            } else {
                                if (lastPoint.y != currentPosition.y) {
                                    if (lastPoint.y - currentPosition.y < 0) {
                                        lastPoint.y + (((lastPoint.y - currentPosition.y) * -1) / steps)
                                    } else {
                                        lastPoint.y - (lastPoint.y - currentPosition.y) / steps
                                    }
                                } else {
                                    currentPosition.y
                                }
                            }.toFloat()


                            val point = Point(currentX.toInt(), currentY.toInt())
                            val lastVertices = verticesList.lastOrNull()?.lastOrNull()?.point
                            val resultDistance =
                                PolygonGeometry.calculateDistance(lastVertices, point)
                            if (verticesList.lastOrNull() != point && resultDistance >= POINTS_RANGE_PX) {
                                verticesList.lastOrNull()?.add(
                                    Vertices(
                                        point
                                    )
                                )

                                fingerLinePath.reset()
                                fingerLinePath.moveTo(
                                    point.x.toFloat(),
                                    point.y.toFloat()
                                ) //!!!
                                fingerLinePath.lineTo(point.x.toFloat(), point.y.toFloat())
                            }

                            addDistance += POINTS_RANGE_PX
                        }

                    } else if (distance.toInt() == POINTS_RANGE_PX) {

                        verticesList.lastOrNull()?.add(
                            Vertices(
                                Point(
                                    currentPosition.x.toInt(),
                                    currentPosition.y.toInt()
                                )
                            )
                        )

                        fingerLinePath.reset()
                        fingerLinePath.moveTo(
                            currentPosition.x,
                            currentPosition.y
                        ) //!!!
                        fingerLinePath.lineTo(currentPosition.x, currentPosition.y)
                    }

                    firstPoint = verticesList.lastOrNull()?.get(0)?.point ?: Point(0, 0)
                    p = viewToSourceCoord(p)
                    x = p!!.x
                    y = p.y

                    val isXaround =
                        x < firstPoint.x + POINTS_RANGE_PX && x > firstPoint.x - POINTS_RANGE_PX
                    val isYaround =
                        y < firstPoint.y + POINTS_RANGE_PX && y > firstPoint.y - POINTS_RANGE_PX

                    if (isXaround && isYaround && (verticesList.lastOrNull()?.size ?: 0) > 2) {
                        val firstPoint = verticesList.lastOrNull()?.first()?.point
                        val lastPoint = verticesList.lastOrNull()?.last()?.point
                        val distance = PolygonGeometry.calculateDistance(lastPoint, firstPoint)
                        if (distance < POINTS_RANGE_PX) {
                            verticesList.lastOrNull()?.dropLast(1)
                        }
                        fingerLinePath.reset()
                        isPathClosed[isPathClosed.lastIndex] = true
                        validateAvailablePoints()
                    }
                }
            }
        } catch (ex: IndexOutOfBoundsException) {
            ex.printStackTrace()
        }
    }

    private fun processClearClick(
        x: Float,
        y: Float,
        closeButton: CloseButton,
        index: Int
    ): Boolean {
        if (closeButton.pointInsideRect(context, index, x, y, mode)) {
            clear(index)
            return true
        }
        return false
    }

    fun clear(index: Int) {
        verticesList.removeAt(index)
        isPathClosed.removeAt(index)
        closeButton.removeAt(index)
        isClear = true
        invalidate()
    }

    private fun doOnDrawWork(canvas: Canvas) {
        if (!isClear) {
            if (showOutlines) {
                verticesList.forEachIndexed { index, vertices ->
                    if (index != this.verticesList.lastIndex && this.verticesList.lastIndex != -1) {
                        drawWidthAndLength(
                            canvas,
                            vertices,
                            widthIndexes?.get(index),
                            lengthIndexes?.get(index)
                        )
                        drawPolygon(canvas, vertices, false) //draw dots
                        closePath(
                            canvas,
                            vertices,
                            isPathClosed[index],
                            false
                        ) //connects first and last points of boundary
                        canvas.drawPath(polygonPath, pathPaint!!) //draw lines between dots
                        if (isNeedWhiteStroke) { //white borders of green dots
                            drawVertexStrokes(canvas, vertices, false)
                        }
                        drawClearSymbol(canvas, closeButton[index], index + 1, vertices)
                        polygonPath.reset()
                    }
                }
                drawPolygon(canvas)
                closePath(
                    canvas,
                    verticesList.lastOrNull() ?: ArrayList(),
                    if (isPathClosed.isEmpty()) {
                        false
                    } else isPathClosed.last(),
                    false
                )
                canvas.drawPath(polygonPath, pathPaint!!)
                if (isNeedWhiteStroke) drawVertexStrokes(
                    canvas,
                    verticesList.lastOrNull() ?: ArrayList(),
                    false
                )
                drawLineByFinger(canvas, verticesList.lastOrNull() ?: ArrayList())
                if (isPathClosed.isNotEmpty()) {
                    if (isTouchUP && !polygonPath.isEmpty && isPathClosed.last()) {
                        if (closeButton.isEmpty()) {
                            closeButton.add(CloseButton(closeButtonRadius.toInt(), context))
                            drawClearSymbol(
                                canvas,
                                closeButton.last(),
                                verticesList.lastIndex + 1,
                                verticesList.lastOrNull() ?: ArrayList()
                            )
                        } else {
                            drawClearSymbol(
                                canvas,
                                closeButton.last(),
                                verticesList.lastIndex + 1,
                                verticesList.lastOrNull() ?: ArrayList()
                            )
                            closeButton.add(CloseButton(closeButtonRadius.toInt(), context))
                        }
                        isTouchUP = false
                        visibilityVerticesIndexes.add(verticesList.lastIndex)
                        isPathClosed.add(false)
                        verticesList.add(ArrayList())
                    }
                }

                polygonPath.reset()
            }
        } else {
            polygonPath.reset()
            fingerLinePath.reset()
            isClear = false
            touchListener!!.onVertexListChanged(verticesList, isAllPathClosed())
            invalidate()
        }
    }

    private fun doOnRepresent(canvas: Canvas) {
        this.verticesList.forEachIndexed { index, vertices ->
            if (vertices.isNotEmpty() && visibilityVerticesIndexes.contains(index)) {
                val widthIndex = widthIndexes?.get(index)
                val lengthIndex = lengthIndexes?.get(index)
                drawWidthAndLength(canvas, vertices, widthIndex, lengthIndex)
                drawPolygon(canvas, vertices, true) //green dots
                closePath(
                    canvas,
                    vertices,
                    isPathClosed[index],
                    true
                ) //connects first and last points of boundary
                canvas.drawPath(polygonPath, pathPaint!!) //green lines between dots
                if (isNeedWhiteStroke) {
                    drawVertexStrokes(canvas, vertices, true)
                } //white borders of green dots

                try {
                    if (closeButton.lastIndex >= index) {
                        if (areaList.lastIndex >= index) {
                            closeButton[index].drawAreaLabel(
                                canvas,
                                greenColor,
                                index + 1,
                                areaList[index],
                                diameter * 10.0,
                                vertices,
                                this,
                                mode
                            )
                        }
                    }
                } catch (_: Exception) {

                }
                polygonPath.reset()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, 0f, 0f, clearPaint!!)
        validateAvailablePoints()
        if (zoom != scale) {
            if (zoom != 0f) {
                zoomChangedTime = System.currentTimeMillis()
            }
            zoom = scale
            closeButton.forEach {
                it.setscale(zoom)
            }
            if (touchListener != null) touchListener!!.onZoomChanged(zoom)
        }
        when (mode) {
            Mode.Draw -> {
                doOnDrawWork(canvas)
            }

            Mode.DrawSingle -> {
                doOnDrawWork(canvas)
            }

            Mode.ViewMeasurement -> {
                doOnRepresent(canvas)
            }

            Mode.ViewStoma -> {
                doOnRepresent(canvas)
            }

            else -> {}
        }
    }

    private fun validateAvailablePoints() {
        if (verticesList.isNotEmpty()) {
            val allowedDistance = this.context.toPx(50)
            isPathClosed.filterIndexed { index, isClosed ->
                if (isClosed) {
                    verticesList[index].let { imagePointCoordinates ->
                        if (scale == maxScale) {
                            imagePointCoordinates.map { it.isEnabled = true }
                        } else {
                            val viewPointCoordinates = imagePointCoordinates.map { vertices ->
                                sourceToViewCoordInt(
                                    Point(
                                        (vertices.point.x),
                                        (vertices.point.y)
                                    )
                                )
                            }
                            var comparePoint = viewPointCoordinates.first()
                            viewPointCoordinates.forEachIndexed { index, point ->
                                val currentDraggedIndex =
                                    viewPointCoordinates.indexOfFirst {
                                        it == sourceToViewCoordInt(
                                            Point(
                                                (currentDraggedVertices?.point?.x ?: 0),
                                                (currentDraggedVertices?.point?.y ?: 0)
                                            )
                                        )
                                    }
                                if (currentDraggedIndex != -1) {
                                    if (index == currentDraggedIndex) {
                                        val prevPoint = if (currentDraggedIndex == 0) {
                                            viewPointCoordinates[viewPointCoordinates.lastIndex]
                                        } else if (currentDraggedIndex == viewPointCoordinates.lastIndex) {
                                            viewPointCoordinates[0]
                                        } else {
                                            viewPointCoordinates[currentDraggedIndex - 1]
                                        }

                                        val distance = PolygonGeometry.calculateDistance(
                                            prevPoint,
                                            point
                                        )
                                        if (distance.toInt() <= allowedDistance) {
                                            val prevIndex =
                                                viewPointCoordinates.indexOfFirst { it == prevPoint }
                                            imagePointCoordinates[prevIndex].isEnabled = false
                                        }
                                        imagePointCoordinates[currentDraggedIndex].isEnabled = true
                                        comparePoint = viewPointCoordinates[index]
                                    } else {
                                        if (comparePoint != point && point != imagePointCoordinates[currentDraggedIndex].point) {
                                            val distance = PolygonGeometry.calculateDistance(
                                                comparePoint,
                                                point
                                            )
                                            imagePointCoordinates[index].isEnabled = distance.toInt() >= allowedDistance
                                            if (distance.toInt() >= allowedDistance) {
                                                comparePoint = viewPointCoordinates[index]
                                            }
                                        } else {
                                            imagePointCoordinates[index].isEnabled = true
                                        }
                                    }
                                } else {
                                    if (comparePoint != point) {
                                        val distance = PolygonGeometry.calculateDistance(comparePoint, point)
                                        imagePointCoordinates[index].isEnabled =
                                            distance.toInt() >= allowedDistance
                                        if (distance.toInt() >= allowedDistance) {
                                            comparePoint = viewPointCoordinates[index]
                                        }
                                    } else {
                                        imagePointCoordinates[index].isEnabled = true
                                    }
                                }
                                if (index == viewPointCoordinates.lastIndex) {
                                    if (index != currentDraggedIndex) {
                                        val firstPoint = viewPointCoordinates.first()
                                        val distance =
                                            PolygonGeometry.calculateDistance(firstPoint, point)
                                        imagePointCoordinates[index].isEnabled =
                                            distance.toInt() >= allowedDistance && imagePointCoordinates[index].isEnabled
                                    }
                                }
                            }
                            val allowedDistance = this.context.toPx(40)
                            val distance = PolygonGeometry.calculateDistance(
                                imagePointCoordinates.last().point,
                                imagePointCoordinates.first().point
                            )
                            imagePointCoordinates[imagePointCoordinates.lastIndex].isEnabled = distance > allowedDistance
                        }
                    }
                }
                isClosed
            }
        }
    }

    private fun drawPolygon(canvas: Canvas) {
        val size = verticesList.lastOrNull()?.size
        if (size != null && size > 0) {
            for (i in 0 until size) {
                val vertex = verticesList.lastOrNull()?.get(i)
                vertex?.let {
                    val point = sourceToViewCoordInt(it.point)
                    if (it.isEnabled) {
                        canvas.drawCircle(
                            point.x.toFloat(),
                            point.y.toFloat(),
                            RADIUS,
                            vertexPaint!!
                        )
                    } else {
                        canvas.drawCircle(
                            point.x.toFloat(),
                            point.y.toFloat(),
                            DISABLED_RADIUS,
                            vertexDisabledPaint!!
                        )
                    }
                    if (i > 0) {
                        polygonPath.lineTo(point.x.toFloat(), point.y.toFloat())
                    } else {
                        polygonPath.moveTo(point.x.toFloat(), point.y.toFloat())
                    }
                    if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                        if (isNeedWhiteStroke && it.isEnabled) {
                            canvas.drawCircle(
                                point.x.toFloat(),
                                point.y.toFloat(),
                                RADIUS,
                                vertexStrokePaint!!
                            )
                        }
                    }
                }
            }
        }
    }

    private fun drawPolygon(
        canvas: Canvas,
        vertices: ArrayList<Vertices>,
        isTimelineFragment: Boolean
    ) {
        val size = vertices.size
        if (size > 0) {
            for (i in 0 until size) {
                val vertex = vertices[i]
                vertex.let {
                    val point = sourceToViewCoordInt(it.point)
                    if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                        if (!isTimelineFragment) {
                            if (it.isEnabled) {
                                canvas.drawCircle(
                                    point.x.toFloat(),
                                    point.y.toFloat(),
                                    RADIUS,
                                    vertexPaint!!
                                )
                            } else {
                                canvas.drawCircle(
                                    point.x.toFloat(),
                                    point.y.toFloat(),
                                    DISABLED_RADIUS,
                                    vertexDisabledPaint!!
                                )
                            }
                        } else {
                            canvas.drawCircle(
                                point.x.toFloat(),
                                point.y.toFloat(),
                                RADIUS,
                                vertexPaint!!
                            )
                        }
                    }

                    if (i > 0) {
                        polygonPath.lineTo(point.x.toFloat(), point.y.toFloat())
                    } else {
                        polygonPath.moveTo(point.x.toFloat(), point.y.toFloat())
                    }
                    if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                        if (isNeedWhiteStroke && it.isEnabled) {
                            canvas.drawCircle(
                                point.x.toFloat(),
                                point.y.toFloat(),
                                RADIUS,
                                vertexStrokePaint!!
                            )
                        }
                    }
                }
            }
        }
    }

    private fun drawWidthAndLength(
        canvas: Canvas,
        vertices: ArrayList<Vertices>,
        widthIndexesPoints: Pair<Int?, Int?>?,
        lengthIndexesPoints: Pair<Int?, Int?>?
    ) {
        //uncomment to show W and L
        if (widthIndexes != null && mode != Mode.ViewStoma) {
            drawLineWithLetter(canvas, vertices, widthIndexesPoints ?: Pair(0, 0), "W")
        }
        if (lengthIndexes != null) {
            drawLineWithLetter(canvas, vertices, lengthIndexesPoints ?: Pair(0, 0), "L")
        }
    }

    private fun drawLineWithLetter(
        canvas: Canvas,
        vertices: ArrayList<Vertices>,
        indexes: Pair<Int?, Int?>,
        letter: String,
    ) {
        try {
            val verticesA = vertices[indexes.first ?: 0]
            val verticesB = vertices[indexes.second ?: 0]
            verticesA.point.let { widthA ->
                val pointA = sourceToViewCoordInt(widthA)

                verticesB.point.let { widthB ->
                    val pointB = sourceToViewCoordInt(widthB)

                    canvas.drawLine(
                        pointA.x.toFloat(),
                        pointA.y.toFloat(),
                        pointB.x.toFloat(),
                        pointB.y.toFloat(),
                        pathPaint!!
                    )
                    var xOffset = 20 + RADIUS * 3.5f
                    if (pointA.x < pointB.x) {
                        xOffset *= -1f
                    }
                    var yOffset = 20 + RADIUS * 3.5f
                    if (pointA.y < pointB.y) {
                        yOffset *= -1f
                    }
                    val offset = Point(xOffset.toInt(), yOffset.toInt())
//                    canvas.drawCircle(
//                        (pointA.x + offset.x).toFloat(),
//                        (pointA.y + offset.y).toFloat(),
//                        RADIUS * 3.5f,
//                        linesStrokePaint!!
//                    )
//                    canvas.drawCircle(
//                        (pointA.x + offset.x).toFloat(),
//                        (pointA.y + offset.y).toFloat(),
//                        RADIUS * 3.5f,
//                        linesPaint!!
//                    )
                    val bounds = Rect()
                    textPaint!!.getTextBounds(letter, 0, letter.length, bounds)
//                    canvas.drawText(
//                        letter,
//                        pointA.x - bounds.width() / 2f + offset.x,
//                        pointA.y + bounds.height() / 2f + offset.y,
//                        textPaint!!
//                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun closePath(
        canvas: Canvas,
        verticesList: ArrayList<Vertices>,
        isPathClosed: Boolean,
        isTimelineFragment: Boolean
    ) {
        if (isPathClosed) {
            val point = verticesList.lastOrNull()?.point
            point?.let {
                val viewPoint = sourceToViewCoordInt(it)
                polygonPath.lineTo(viewPoint.x.toFloat(), viewPoint.y.toFloat())
                polygonPath.close()
                if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                    canvas.drawPath(polygonPath, fillPathPaint!!)
                    if (isNeedWhiteStroke) {
                        drawVertexStrokes(canvas, verticesList, isTimelineFragment)
                    }
                }
            }
        }
    }

    private fun drawLineByFinger(
        canvas: Canvas,
        vertices: ArrayList<Vertices>
    ) {
        if (isPathClosed.isNotEmpty()) {
            if (!isPathClosed.last()) {
                canvas.drawPath(fingerLinePath, pathPaint!!)
                if (isNeedWhiteStroke) {
                    drawVertexStrokes(canvas, vertices, false)
                }
            }
        }
    }

    private fun drawClearSymbol(
        canvas: Canvas,
        closeButton: CloseButton,
        number: Int,
        vertices: ArrayList<Vertices>
    ) {
        closeButton.drawFilled(canvas, greenColor, number, vertices, this, mode)
    }

    fun sourceToViewCoordInt(vertex: Point): Point {
        val pointF = sourceToViewCoord(vertex.x.toFloat(), vertex.y.toFloat())
            ?: return Point(0, 0)
        return Point(pointF.x.toInt(), pointF.y.toInt())
    }

    fun viewToSourceCoordInt(vertex: Point): Point {
        val pointF = viewToSourceCoord(vertex.x.toFloat(), vertex.y.toFloat())
        return Point(pointF!!.x.toInt(), pointF.y.toInt())
    }


    fun setVisibilityVerticesIndexes(visibilityVerticesIndexes: List<Int>) {
        this.visibilityVerticesIndexes = ArrayList(visibilityVerticesIndexes)
        showOutlines = visibilityVerticesIndexes.isNotEmpty()
        invalidate()
    }

    fun setAutoDetectedVertices(vertices: List<List<Vertices>>) {
        this.verticesList = validateAutoDetectionVertices(vertices)
        if (this.verticesList.isEmpty()) {
            isPathClosed.clear()
            closeButton.clear()
            closeButton.add(CloseButton(closeButtonRadius.toInt(), context))
            isClear = true
            if (touchListener != null) touchListener!!.onUp()
            visibilityVerticesIndexes = ArrayList(emptyList())
            postInvalidate()
        } else {
            isPathClosed = ArrayList(this.verticesList.map { it.isNotEmpty() && it.size >= 3 })
            closeButton =
                ArrayList(this.verticesList.map { CloseButton(closeButtonRadius.toInt(), context) })
            if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                isTouchUP = true
            }
            visibilityVerticesIndexes = ArrayList(verticesList.mapIndexed { index, _ -> index })
        }
        showOutlines = true
        validateAvailablePoints()
        touchListener?.onVertexListChanged(this.verticesList, isAllPathClosed())
        invalidate()
    }

    fun setVertices(vertices: ArrayList<ArrayList<Vertices>>) {
        this.verticesList = vertices

        if (this.verticesList.isEmpty()) {
            isPathClosed.clear()
            closeButton.clear()
            closeButton.add(CloseButton(closeButtonRadius.toInt(), context))
            isClear = true
            if (touchListener != null) touchListener!!.onUp()
            visibilityVerticesIndexes = ArrayList(emptyList())
            postInvalidate()
        } else {
            isPathClosed = ArrayList(this.verticesList.map { it.isNotEmpty() && it.size >= 3 })
            closeButton =
                ArrayList(this.verticesList.map { CloseButton(closeButtonRadius.toInt(), context) })
            if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                isTouchUP = true
            }
            visibilityVerticesIndexes = ArrayList(verticesList.mapIndexed { index, _ -> index })
        }
        touchListener?.onVertexListChanged(this.verticesList, isAllPathClosed())
        invalidate()
    }

    private fun validateAutoDetectionVertices(verticesList: List<List<Vertices>>): ArrayList<ArrayList<Vertices>> {

        val viewVerticesList = verticesList.map {
            it.map { vertix ->
                PointF(
                    (vertix.point.x.toFloat()),
                    (vertix.point.y.toFloat())
                )
            }
        }

        val orderedVerticesList = ArrayList<ArrayList<PointF>>()

        viewVerticesList.forEach { vertices ->
            if (vertices.isNotEmpty()) {
                orderedVerticesList.add(ArrayList())

                vertices.forEach { vertix ->
                    if (vertices[0] == vertix) {
                        orderedVerticesList.lastOrNull()?.add(vertix)
                    } else {
                        var compareVertices = vertix
                        val distances = SparseArray<PointF?>()
                        val nearestPoint: PointF?
                        val allowedDistance = this.context.toPx(40)
                        vertices.forEach { vertix ->
                            if (vertix != compareVertices) {
                                val calculateDistance =
                                    PolygonGeometry.calculateDistance(
                                        compareVertices,
                                        vertix
                                    ).toInt()
                                if (calculateDistance < allowedDistance) {
                                    distances.put(calculateDistance, vertix)
                                }
                            }
                        }
                        if (distances.size() != 0) {
                            val keyAt = distances.keyAt(0)
                            nearestPoint = distances[keyAt, null]
                            if (nearestPoint != null) {
                                if (orderedVerticesList.lastOrNull()?.contains(nearestPoint) == false
                                ) {
                                    orderedVerticesList.lastOrNull()?.add(nearestPoint)
                                    compareVertices = nearestPoint
                                }
                            }
                        }
                    }
                }
            }
        }

        val adjustedVertices = ArrayList<Pair<Int, ArrayList<Int>>>()
        orderedVerticesList.forEachIndexed { verticesIndex, vertices ->
            val removePointIndexes = ArrayList<Int>()
            if (vertices.isNotEmpty()) {
                var compareVertices = vertices.first()
                vertices.forEachIndexed { index, vertix ->
                    if (compareVertices != vertix) {
                        val distance = PolygonGeometry.calculateDistance(
                            compareVertices,
                            vertix
                        )
                        if (distance.toInt() < POINTS_RANGE_PX) {
                            removePointIndexes.add(index)
                        }
                    }
                    compareVertices = vertix
                }

                val distance = PolygonGeometry.calculateDistance(
                    vertices.first(),
                    vertices.last()
                )
                if (distance.toInt() < POINTS_RANGE_PX) {
                    removePointIndexes.add(0)
                }
                if (removePointIndexes.isNotEmpty()) {
                    if (!adjustedVertices.contains(Pair(verticesIndex, removePointIndexes))) {
                        adjustedVertices.add(Pair(verticesIndex, removePointIndexes))
                    }
                }
            }
        }

        adjustedVertices.forEach { (verticesIndex, removeIndexes) ->
            if (orderedVerticesList[verticesIndex].size > 3) {
                removeIndexes.distinct().sortedByDescending { it }.forEach {
                    orderedVerticesList[verticesIndex].removeAt(it)
                }
            }
        }

        val extendedVerticesList = ArrayList<ArrayList<Vertices>>()

        orderedVerticesList.forEach { vertices ->
            if (vertices.isNotEmpty()) {
                val pointList = ArrayList<Vertices>()

                var compareVertices = vertices[0]
                vertices.forEach { vertix ->
                    if (compareVertices == vertix) {
                        pointList.add(Vertices(vertix))
                    } else {
                        val distance = PolygonGeometry.calculateDistance(compareVertices, vertix)
                        compareVertices =
                            if (distance.toInt() >= POINTS_RANGE_PX) {
                                val result = addAdditionalDotsBetweenAutoDetectionPoints(
                                    compareVertices,
                                    vertix
                                )
                                result.forEach {
                                    if (!pointList.contains(
                                            Vertices(
                                                it
                                            )
                                        )) {
                                        pointList.add(
                                            Vertices(
                                                it
                                            )
                                        )
                                    }
                                }
                                PointF(pointList.last().point)
                            } else {
                                pointList.add(
                                    Vertices(
                                        vertix
                                    )
                                )
                                vertix
                            }
                    }
                }

                extendedVerticesList.add(pointList)
            }
        }

        return ArrayList(extendedVerticesList.map { ArrayList(it) })
    }

    fun setWidthAndLength(
        widthIndexes: ArrayList<Pair<Int?, Int?>>,
        lengthIndexes: ArrayList<Pair<Int?, Int?>>,
        areaList: List<Double> = emptyList()
    ) {
        this.widthIndexes = widthIndexes
        this.lengthIndexes = lengthIndexes
        this.areaList = ArrayList(areaList)
        invalidate()
    }

    fun isNeedFillPolygon(isNeedFill: Boolean) {
        if (isNeedFill) {
            fillPathPaint!!.style = Paint.Style.FILL
        } else {
            fillPathPaint!!.style = Paint.Style.STROKE
        }
    }

    fun isNeedWhiteStrokesOnVertex(isNeedStrokes: Boolean) {
        isNeedWhiteStroke = isNeedStrokes
    }

    private fun drawVertexStrokes(
        canvas: Canvas,
        verticesList: ArrayList<Vertices>,
        isTimelineFragment: Boolean
    ) {
        val size = verticesList.size
        if (size > 0) {
            for (i in 0 until size) {
                val vertices = verticesList[i]
                vertices.let { vertex ->
                    val point = sourceToViewCoordInt(vertex.point)
                    if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                        if (!isTimelineFragment && vertex.isEnabled) {
                            canvas.drawCircle(
                                point.x.toFloat(),
                                point.y.toFloat(),
                                RADIUS,
                                vertexStrokePaint!!
                            )
                        }
                    }
                }
            }
        }
    }

    interface ViewTouchListener {
        fun onDown(sourceCoords: PointF?)
        fun onMove(viewCoord: PointF?)
        fun onUp()
        fun onVertexListChanged(vertices: ArrayList<ArrayList<Vertices>>?, closed: Boolean)
        fun onZoomChanged(zoom: Float)
    }

    enum class Mode {
        Draw, DrawSingle, View, ViewMeasurement, ViewStoma
    }

    companion object {
        var RADIUS = 10f
        var DISABLED_RADIUS = 8f
        const val POINTS_RANGE_PX = 35
        private const val FIRST_TWO_POINTS_MULTIPLIER = 0.5
        private const val SECOND_TWO_POINTS_MULTIPLIER = 0.2
    }

}