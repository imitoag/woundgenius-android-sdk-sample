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
import com.example.samplewoundsdk.data.pojo.measurement.AreaButton
import com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint
import com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges
import com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition
import com.example.samplewoundsdk.data.pojo.measurement.Vertices
import com.example.samplewoundsdk.data.pojo.measurement.VerticesVector
import com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButton
import com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition
import com.example.samplewoundsdk.utils.image.drawstroke.CloseButton
import com.example.samplewoundsdk.utils.image.drawstroke.PolygonGeometry
import timber.log.Timber
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class StrokeScalableImageView : SubsamplingScaleImageView {

    private var closeButtonRadius = 0f
    var textSize = 0f
    private var lineWidth = 0f
    var minDistance = 0f
    private var greenColor = 0
    private var transparentGreenColor = 0
    private val polygonPath = Path()
    private val fingerLinePath = Path()
    var verticesList =
        ArrayList<ArrayList<Vertices>>()
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
    private var currentDraggedVertices: Vertices? =
        null
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
    private var lengthWidthLabelCoordinates =
        ArrayList<Pair<LengthWidthButton?, LengthWidthButton?>>()

    private var clearButtonLabelCoordinates = ArrayList<AreaButton>()
    private var clearButtonRectangleCoordinates = ArrayList<Pair<Int, Pair<Int, Int>>>()

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
        closeButtonRadius =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.stroke_circle_radius)
        textSize =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.stroke_text_size)
        lineWidth =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.stroke_line_width)
        minDistance =
            context.resources.getDimension(com.example.samplewoundsdk.R.dimen.stroke_min_distance)
        greenColor = ContextCompat.getColor(
            context,
            com.example.samplewoundsdk.R.color.sample_app_color_green
        )
        transparentGreenColor =
            ContextCompat.getColor(
                context,
                com.example.samplewoundsdk.R.color.sample_app_color_green_transparent
            )
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
        linesPaint!!.color = Color.WHITE
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint!!.color = Color.GREEN
        textPaint!!.textSize = 42f
        textPaint!!.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        linesStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linesStrokePaint!!.color =
            resources.getColor(com.example.samplewoundsdk.R.color.sample_app_color_transparent)
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

        val newPointsForFirstAdjustedList =
            ArrayList<Vertices>()
        val newPointsForSecondAdjustedList =
            ArrayList<Vertices>()

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

    private fun getCenterPointOnLine(startPoint: Point, endPoint: Point): Point {
        val newPointX = (startPoint.x + endPoint.x) / 2
        val newPointY = (startPoint.y + endPoint.y) / 2
        return Point(newPointX, newPointY)
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
                    currentDraggedVertices = null
                    return
                }
            }
        }
        if (currentDraggedVertices == null && !isClear) {
            if (verticesList.size > 1 && mode == Mode.DrawSingle) {
                return
            }
            if (verticesList.size == 0) {
                verticesList =
                    ArrayList<ArrayList<Vertices>>().apply {
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
                                        verticesList.lastOrNull()?.let {
                                            checkIfAllAreaValid(it)
                                        }
                                        checkIfLabelInsideAnyOutlines()
                                        validateAvailablePoints()
                                    }
                                }
                            }
                        }
                    } else {
                        if (verticesList.isNotEmpty() && verticesList.lastOrNull()
                                ?.isEmpty() == true
                        ) {
                            verticesList.lastOrNull()?.add(
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
            verticesList.lastOrNull()?.let {
                checkIfAllAreaValid(it)
            }
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

    private fun checkIfAllAreaValid(vertices: ArrayList<Vertices>) {
        if (vertices.size > 1) {
            val preLast = vertices.lastIndex - 1
            val verticePointA = sourceToViewCoordInt(vertices[preLast].point)
            val verticePointB = sourceToViewCoordInt(vertices.last().point)

            clearButtonRectangleCoordinates.forEachIndexed { buttonIndex, rectanglePoints ->
                val pointIndex = clearButtonLabelCoordinates[buttonIndex].areaIndex
                val currentOutlinePoint =
                    sourceToViewCoordInt(verticesList[buttonIndex][pointIndex].point)

                val pointXMoveDistance = currentOutlinePoint.x - rectanglePoints.second.first
                val pointYMoveDistance = currentOutlinePoint.y - rectanglePoints.second.second

                val rectanglePoint = Point(pointXMoveDistance, pointYMoveDistance)

                val rectanglePointList = getAreaClearRectangleLabelPoints(
                    rectanglePoint,
                    rectanglePoints.first,
                    closeButton[buttonIndex],
                    buttonIndex + 1
                )

                if (isLabelCrossAnyOutlines(rectanglePointList)) {
                    clearButtonLabelCoordinates[buttonIndex] =
                        clearButtonLabelCoordinates[buttonIndex].copy(isAreaValid = false)
                }


                rectanglePointList.forEachIndexed { index, point ->
                    val pointA = sourceToViewCoordInt(point)
                    val pointB = if (index + 1 > rectanglePointList.lastIndex) {
                        sourceToViewCoordInt(rectanglePointList[0])
                    } else {
                        sourceToViewCoordInt(rectanglePointList[index + 1])
                    }

                    if (linesIntersect(verticePointA, verticePointB, pointA, pointB)) {
                        clearButtonLabelCoordinates[buttonIndex] =
                            clearButtonLabelCoordinates[buttonIndex].copy(isAreaValid = false)
                    }
                }
            }
        }
    }

    private fun checkIfLabelInsideAnyOutlines() {
        clearButtonRectangleCoordinates.forEachIndexed { buttonIndex, rectanglePoints ->
            val pointIndex = clearButtonLabelCoordinates[buttonIndex].areaIndex
            val currentOutlinePoint =
                sourceToViewCoordInt(verticesList[buttonIndex][pointIndex].point)

            val pointXMoveDistance = currentOutlinePoint.x - rectanglePoints.second.first
            val pointYMoveDistance = currentOutlinePoint.y - rectanglePoints.second.second

            val rectanglePoint = Point(pointXMoveDistance, pointYMoveDistance)

            val rectanglePointList = getAreaClearRectangleLabelPoints(
                rectanglePoint,
                rectanglePoints.first,
                closeButton[buttonIndex],
                buttonIndex + 1
            )

            if (isLabelCrossAnyOutlines(rectanglePointList)) {
                clearButtonLabelCoordinates[buttonIndex] =
                    clearButtonLabelCoordinates[buttonIndex].copy(isAreaValid = false)
            }
        }
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
                                        verticesList.lastOrNull()?.let {
                                            checkIfAllAreaValid(it)
                                        }
                                        checkIfLabelInsideAnyOutlines()
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
            verticesList.lastOrNull()?.let {
                checkIfAllAreaValid(it)
            }
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


    private fun touchMove(x: Float, y: Float) {
        if (isPointCanBeMoved() && currentDraggedVertices != null) {
            movePointsToSameDirection(x, y)
            verticesList.lastOrNull()?.let {
                checkIfAllAreaValid(it)
            }
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

            var newPoints =
                Pair<List<Vertices>, List<Vertices>>(
                    emptyList(),
                    emptyList()
                )
            var adjustedVertices: ArrayList<Vertices>? =
                null

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
                        if (distance.toInt() + 5 < POINTS_RANGE_PX) {
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
                if (distance.toInt() + 5 < POINTS_RANGE_PX) {
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

    private fun findNearestPoint(
        x: Float,
        y: Float
    ): Vertices? {
        val distances = SparseArray<Vertices?>()
        val position = Point(x.toInt(), y.toInt())
        val nearestPoint: Vertices?

        val allowedDistance = this.context.toPx(40)
        verticesList.forEach { vertices ->
            vertices.filter { it.isEnabled }.forEach { vertix ->
                val compareVerticesInView = sourceToViewCoordInt(position)
                val vertixInView = sourceToViewCoordInt(vertix.point)

                val calculateDistance =
                    PolygonGeometry.calculateDistance(
                        compareVerticesInView,
                        vertixInView
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

                                verticesList.lastOrNull()?.let {
                                    checkIfAllAreaValid(it)
                                }

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

                        verticesList.lastOrNull()?.let {
                            checkIfAllAreaValid(it)
                        }

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
                        verticesList.lastOrNull()?.let {
                            checkIfAllAreaValid(it)
                        }
                        checkIfLabelInsideAnyOutlines()
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
        clearButtonLabelCoordinates.removeAt(index)
        clearButtonRectangleCoordinates.removeAt(index)
        isClear = true
        invalidate()
    }

    private fun doOnDrawWork(canvas: Canvas) {
        if (!isClear) {
            if (showOutlines) {
                verticesList.forEachIndexed { index, vertices ->
                    if (index != this.verticesList.lastIndex && this.verticesList.lastIndex != -1) {
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
                        drawAreaLabels(index, canvas, vertices, true)
                        polygonPath.reset()
                    }
                }
                verticesList.forEachIndexed { index, vertices ->
                    if (index != this.verticesList.lastIndex && this.verticesList.lastIndex != -1) {
                        drawOrientationLine(
                            canvas,
                            vertices,
                            widthIndexes?.get(index),
                            lengthIndexes?.get(index)
                        )
                    }
                }
                verticesList.forEachIndexed { index, vertices ->
                    if (index != this.verticesList.lastIndex && this.verticesList.lastIndex != -1) {
                        drawWidthAndLengthLabels(
                            canvas,
                            vertices,
                            widthIndexes?.get(index),
                            lengthIndexes?.get(index),
                            index
                        )
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
                            drawAreaLabels(
                                verticesList.lastIndex,
                                canvas,
                                verticesList.lastOrNull() ?: ArrayList(),
                                true
                            )
                        } else {
                            drawAreaLabels(
                                verticesList.lastIndex,
                                canvas,
                                verticesList.lastOrNull() ?: ArrayList(),
                                true
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
                polygonPath.reset()
            }
        }
        verticesList.forEachIndexed { index, vertices ->
            if (vertices.isNotEmpty() && visibilityVerticesIndexes.contains(index)) {
                drawOrientationLine(
                    canvas,
                    vertices,
                    widthIndexes?.get(index),
                    lengthIndexes?.get(index)
                )
            }
        }
        this.verticesList.forEachIndexed { index, vertices ->
            if (vertices.isNotEmpty() && visibilityVerticesIndexes.contains(index)) {
                val widthIndex = widthIndexes?.get(index)
                val lengthIndex = lengthIndexes?.get(index)
                drawWidthAndLengthLabels(canvas, vertices, widthIndex, lengthIndex, index)
            }
        }
        this.verticesList.forEachIndexed { index, vertices ->
            if (vertices.isNotEmpty() && visibilityVerticesIndexes.contains(index)) {
                try {
                    if (closeButton.lastIndex >= index) {
                        if (areaList.lastIndex >= index) {
                            drawAreaLabels(index, canvas, vertices, false)
                        }
                    }
                } catch (_: Exception) {

                }
            }
        }
    }

    private fun moveLengthWidthLabelIfNeed(
        canvas: Canvas,
        labelPoint: Point, // view coordinates
        startIndex: Int,
        letter: String,
        index: Int,
        isLengthLabel: Boolean,
        vertices: List<Vertices>
    ) {
        val orientationLabel: Point
        var pointIndex = startIndex
        var pointDifference = Pair(0, 0)
        var endTime: Long = System.currentTimeMillis()

        if (lengthWidthLabelCoordinates.isNotEmpty()) {
            if (index <= lengthWidthLabelCoordinates.lastIndex) {
                if (isLengthLabel) {
                    if (lengthWidthLabelCoordinates[index].first != null) {
                        pointIndex = lengthWidthLabelCoordinates[index].first?.outlinePointIndex!!

                        val outlinePoint =
                            sourceToViewCoordInt(verticesList[index][pointIndex].point)
                        pointDifference = lengthWidthLabelCoordinates[index].first?.drawDifference!!

                        val xDifference = pointDifference.first
                        val yDifference = pointDifference.second

                        val labelPoint = Point(
                            outlinePoint.x - xDifference,
                            outlinePoint.y - yDifference
                        )
                        orientationLabel = labelPoint
                    } else {
                        orientationLabel = labelPoint
                    }
                } else {
                    if (lengthWidthLabelCoordinates[index].second != null) {
                        pointIndex = lengthWidthLabelCoordinates[index].second?.outlinePointIndex!!

                        val outlinePoint =
                            sourceToViewCoordInt(verticesList[index][pointIndex].point)
                        pointDifference =
                            lengthWidthLabelCoordinates[index].second?.drawDifference!!

                        val xDifference = pointDifference.first
                        val yDifference = pointDifference.second

                        val labelPoint = Point(
                            outlinePoint.x - xDifference,
                            outlinePoint.y - yDifference
                        )
                        orientationLabel = labelPoint
                    } else {
                        orientationLabel = labelPoint
                    }
                }
            } else {
                orientationLabel = labelPoint
            }
        } else {
            orientationLabel = labelPoint
        }

        val functionStartTime = System.currentTimeMillis()
        var duration: Long

        var isPointValid = false

        if (lengthWidthLabelCoordinates.isNotEmpty()) {
            if (index <= lengthWidthLabelCoordinates.lastIndex) {
                isPointValid = if (isLengthLabel) {
                    lengthWidthLabelCoordinates[index].first?.isAreaValid ?: false
                } else {
                    lengthWidthLabelCoordinates[index].second?.isAreaValid ?: false
                }
            }
        }

        if (isPointValid) {
            drawLabelCircle(canvas, orientationLabel, letter)
        } else {
            var methodStartTime: Long = System.currentTimeMillis()
            Timber.tag("moveLengthWidthLabelIfNeed func")
                .d("start")

            val crossAnyLengthWidthLabel =
                isOrientationLabelCrossAnyOrientationLabel(orientationLabel, index)

            duration = endTime - methodStartTime
            Timber.tag("moveLengthWidthLabelIfNeed")
                .d("checkIfOrientationLabelCrossAnyOrientationLabel duration time = $duration ms")

            val allowedDistance = RADIUS * 2f

            methodStartTime = System.currentTimeMillis()

            val isNewLabelInsideAnyOutlines =
                isLengthWidthLabelInsideAnyOutlines(orientationLabel, allowedDistance)
            endTime = System.currentTimeMillis()
            duration = endTime - methodStartTime
            Timber.tag("moveLengthWidthLabelIfNeed")
                .d("checkIfLengthWidthLabelInsideAnyOutlines duration time = $duration ms")

            if (crossAnyLengthWidthLabel.first && crossAnyLengthWidthLabel.second != null || isNewLabelInsideAnyOutlines) {
                val newLabelPoint = moveOrientationLabel(
                    pointIndex,
                    allowedDistance,
                    vertices,
                    index
                )
                drawLabelCircle(canvas, newLabelPoint.point, letter)
                val pointDifference = newLabelPoint.drawDifference
                val outlinePointIndex = newLabelPoint.outlinePointIndex
                if (lengthWidthLabelCoordinates.isEmpty()) {
                    if (isLengthLabel) {
                        lengthWidthLabelCoordinates.add(
                            Pair(
                                LengthWidthButton(
                                    outlinePointIndex,
                                    pointDifference,
                                    true
                                ), null
                            )
                        )
                    } else {
                        lengthWidthLabelCoordinates.add(
                            Pair(
                                null,
                                LengthWidthButton(
                                    outlinePointIndex,
                                    pointDifference,
                                    true
                                )
                            )
                        )
                    }
                } else {
                    if (index <= lengthWidthLabelCoordinates.lastIndex) {
                        if (isLengthLabel) {
                            lengthWidthLabelCoordinates[index] =
                                Pair(
                                    LengthWidthButton(
                                        outlinePointIndex,
                                        pointDifference,
                                        true
                                    ),
                                    lengthWidthLabelCoordinates[index].second
                                )
                        } else {
                            lengthWidthLabelCoordinates[index] =
                                Pair(
                                    lengthWidthLabelCoordinates[index].first,
                                    LengthWidthButton(
                                        outlinePointIndex,
                                        pointDifference,
                                        true
                                    )
                                )
                        }
                    } else {
                        if (isLengthLabel) {
                            lengthWidthLabelCoordinates.add(
                                Pair(
                                    LengthWidthButton(
                                        outlinePointIndex,
                                        pointDifference,
                                        true
                                    ),
                                    null
                                )
                            )
                        } else {
                            lengthWidthLabelCoordinates.add(
                                Pair(
                                    null,
                                    LengthWidthButton(
                                        outlinePointIndex,
                                        pointDifference,
                                        true
                                    )
                                )
                            )
                        }
                    }
                }
            } else {
                if (orientationLabel.x in 0..<width && orientationLabel.y in 0..<height) {
                    drawLabelCircle(canvas, orientationLabel, letter)
                    if (lengthWidthLabelCoordinates.isEmpty()) {
                        if (isLengthLabel) {
                            lengthWidthLabelCoordinates.add(
                                Pair(
                                    LengthWidthButton(
                                        pointIndex,
                                        pointDifference,
                                        true
                                    ),
                                    null
                                )
                            )
                        } else {
                            lengthWidthLabelCoordinates.add(
                                Pair(
                                    null,
                                    LengthWidthButton(
                                        pointIndex,
                                        pointDifference,
                                        true
                                    )
                                )
                            )
                        }
                    } else {
                        if (index <= lengthWidthLabelCoordinates.lastIndex) {
                            if (isLengthLabel) {
                                lengthWidthLabelCoordinates[index] =
                                    Pair(
                                        LengthWidthButton(
                                            pointIndex,
                                            pointDifference,
                                            true
                                        ),
                                        lengthWidthLabelCoordinates[index].second
                                    )
                            } else {
                                lengthWidthLabelCoordinates[index] =
                                    Pair(
                                        lengthWidthLabelCoordinates[index].first,
                                        LengthWidthButton(
                                            pointIndex,
                                            pointDifference,
                                            true
                                        )
                                    )
                            }
                        } else {
                            if (isLengthLabel) {
                                lengthWidthLabelCoordinates.add(
                                    Pair(
                                        LengthWidthButton(
                                            pointIndex,
                                            pointDifference,
                                            true
                                        ),
                                        null
                                    )
                                )
                            } else {
                                lengthWidthLabelCoordinates.add(
                                    Pair(
                                        null,
                                        LengthWidthButton(
                                            pointIndex,
                                            pointDifference,
                                            true
                                        )
                                    )
                                )
                            }
                        }
                    }
                } else {
                    val newLabelPoint = moveOrientationLabel(
                        pointIndex,
                        allowedDistance,
                        vertices,
                        index
                    )
                    drawLabelCircle(canvas, newLabelPoint.point, letter)
                    if (lengthWidthLabelCoordinates.isEmpty()) {
                        if (isLengthLabel) {
                            lengthWidthLabelCoordinates.add(
                                Pair(
                                    LengthWidthButton(
                                        newLabelPoint.outlinePointIndex,
                                        newLabelPoint.drawDifference,
                                        true
                                    ),
                                    null
                                )
                            )
                        } else {
                            lengthWidthLabelCoordinates.add(
                                Pair(
                                    null,
                                    LengthWidthButton(
                                        newLabelPoint.outlinePointIndex,
                                        newLabelPoint.drawDifference,
                                        true
                                    )
                                )
                            )
                        }
                    } else {
                        if (index <= lengthWidthLabelCoordinates.lastIndex) {
                            if (isLengthLabel) {
                                lengthWidthLabelCoordinates[index] =
                                    Pair(
                                        LengthWidthButton(
                                            newLabelPoint.outlinePointIndex,
                                            newLabelPoint.drawDifference,
                                            true
                                        ),
                                        lengthWidthLabelCoordinates[index].second
                                    )
                            } else {
                                lengthWidthLabelCoordinates[index] =
                                    Pair(
                                        lengthWidthLabelCoordinates[index].first,
                                        LengthWidthButton(
                                            newLabelPoint.outlinePointIndex,
                                            newLabelPoint.drawDifference,
                                            true
                                        )
                                    )
                            }
                        } else {
                            if (isLengthLabel) {
                                lengthWidthLabelCoordinates.add(
                                    Pair(
                                        LengthWidthButton(
                                            newLabelPoint.outlinePointIndex,
                                            newLabelPoint.drawDifference,
                                            true
                                        ),
                                        null
                                    )
                                )
                            } else {
                                lengthWidthLabelCoordinates.add(
                                    Pair(
                                        null,
                                        LengthWidthButton(
                                            newLabelPoint.outlinePointIndex,
                                            newLabelPoint.drawDifference,
                                            true
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        endTime = System.currentTimeMillis()
        duration = endTime - functionStartTime
        Timber.tag("moveLengthWidthLabelIfNeed func").d("duration time = $duration")
    }


    private fun isAreaLabelCrossAnyOrientationLabels(rectanglePointList: ArrayList<Point>): Boolean {
        val allowDistance = RADIUS * 2
        val firstRectangleEdges =
            com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromRectangle(
                rectanglePointList
            )

        for (i in 0..lengthWidthLabelCoordinates.lastIndex step 1) {
            lengthWidthLabelCoordinates[i].first?.let {
                val outlinePoint = sourceToViewCoordInt(verticesList[i][it.outlinePointIndex].point)

                val xDifference = outlinePoint.x - it.drawDifference.first
                val yDifference = outlinePoint.y - it.drawDifference.second

                val lengthLabelPointToView = Point(xDifference, yDifference)

                val secondRectangleEdges =
                    com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromCircle(
                        lengthLabelPointToView,
                        allowDistance
                    )
                if (checkIfLabelTouchOrientationCircle(secondRectangleEdges, firstRectangleEdges)) {
                    return true
                }
            }
            lengthWidthLabelCoordinates[i].second?.let {
                val outlinePoint = sourceToViewCoordInt(verticesList[i][it.outlinePointIndex].point)

                val xDifference = outlinePoint.x - it.drawDifference.first
                val yDifference = outlinePoint.y - it.drawDifference.second

                val widthLabelPointToView = Point(xDifference, yDifference)

                val secondRectangleEdges =
                    com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromCircle(
                        widthLabelPointToView,
                        allowDistance
                    )
                if (checkIfLabelTouchOrientationCircle(secondRectangleEdges, firstRectangleEdges)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkIfLabelTouchOrientationCircle(
        firstEdgeList: com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges,
        secondEdgeList: com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges
    ): Boolean {

        if (firstEdgeList.rightEdge > secondEdgeList.leftEdge && firstEdgeList.rightEdge < secondEdgeList.rightEdge) {
            if (firstEdgeList.topEdge > secondEdgeList.topEdge && firstEdgeList.topEdge < secondEdgeList.bottomEdge) {
                return true
            }
            if (firstEdgeList.bottomEdge > secondEdgeList.topEdge && firstEdgeList.bottomEdge < secondEdgeList.bottomEdge) {
                return true
            }
        }

        if (firstEdgeList.bottomEdge > secondEdgeList.topEdge && firstEdgeList.bottomEdge < secondEdgeList.bottomEdge) {
            if (firstEdgeList.leftEdge > secondEdgeList.leftEdge && firstEdgeList.leftEdge < secondEdgeList.rightEdge) {
                return true
            }
            if (firstEdgeList.rightEdge > secondEdgeList.leftEdge && firstEdgeList.rightEdge < secondEdgeList.rightEdge) {
                return true
            }
        }

        if (firstEdgeList.leftEdge > secondEdgeList.leftEdge && firstEdgeList.leftEdge < secondEdgeList.rightEdge) {
            if (firstEdgeList.topEdge > secondEdgeList.topEdge && firstEdgeList.topEdge < secondEdgeList.bottomEdge) {
                return true
            }
            if (firstEdgeList.bottomEdge > secondEdgeList.topEdge && firstEdgeList.bottomEdge < secondEdgeList.bottomEdge) {
                return true
            }
        }

        if (firstEdgeList.topEdge > secondEdgeList.topEdge && firstEdgeList.topEdge < secondEdgeList.bottomEdge) {
            if (firstEdgeList.leftEdge > secondEdgeList.leftEdge && firstEdgeList.leftEdge < secondEdgeList.rightEdge) {
                return true
            }
            if (firstEdgeList.rightEdge > secondEdgeList.leftEdge && firstEdgeList.rightEdge < secondEdgeList.rightEdge) {
                return true
            }
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, 0f, 0f, clearPaint!!)
        validateAvailablePoints()
        val currentZoom = String.format(
            Locale.UK,
            context.getString(com.example.samplewoundsdk.R.string.float_format_two_points),
            zoom
        ).toDouble()
        val currentScale = String.format(
            Locale.UK,
            context.getString(com.example.samplewoundsdk.R.string.float_format_two_points),
            scale
        ).toDouble()
        if (currentZoom != currentScale) {
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
                                            imagePointCoordinates[index].isEnabled =
                                                distance.toInt() >= allowedDistance
                                            if (distance.toInt() >= allowedDistance) {
                                                comparePoint = viewPointCoordinates[index]
                                            }
                                        } else {
                                            imagePointCoordinates[index].isEnabled = true
                                        }
                                    }
                                } else {
                                    if (comparePoint != point) {
                                        val distance =
                                            PolygonGeometry.calculateDistance(comparePoint, point)
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
                            imagePointCoordinates[imagePointCoordinates.lastIndex].isEnabled =
                                distance > allowedDistance
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

    private fun drawWidthAndLengthLabels(
        canvas: Canvas,
        vertices: ArrayList<Vertices>,
        widthIndexesPoints: Pair<Int?, Int?>?,
        lengthIndexesPoints: Pair<Int?, Int?>?,
        index: Int
    ) {
        //uncomment to show W and L
        if (widthIndexes != null && mode != Mode.ViewStoma) {
            drawLengthWidthCircleLabel(
                canvas,
                vertices,
                widthIndexesPoints ?: Pair(0, 0),
                context.getString(com.example.samplewoundsdk.R.string.WIDTH_SHORT),
                index,
                false
            )
        }
        if (lengthIndexes != null) {
            drawLengthWidthCircleLabel(
                canvas,
                vertices,
                lengthIndexesPoints ?: Pair(0, 0),
                context.getString(com.example.samplewoundsdk.R.string.LENGTH_SHORT),
                index,
                true
            )
        }
    }

    private fun drawOrientationLine(
        canvas: Canvas,
        vertices: ArrayList<Vertices>,
        widthIndexes: Pair<Int?, Int?>?,
        lengthIndexes: Pair<Int?, Int?>?
    ) {
        if (widthIndexes != null && mode != Mode.ViewStoma) {
            try {
                val verticesA = vertices[widthIndexes.first ?: 0]
                val verticesB = vertices[widthIndexes.second ?: 0]
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
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        lengthIndexes?.let {
            try {
                val verticesA = vertices[lengthIndexes.first ?: 0]
                val verticesB = vertices[lengthIndexes.second ?: 0]
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
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun drawLengthWidthCircleLabel(
        canvas: Canvas,
        vertices: ArrayList<Vertices>,
        indexes: Pair<Int?, Int?>,
        letter: String,
        index: Int,
        isLengthLabel: Boolean
    ) {
        try {
            val verticesA = vertices[indexes.first ?: 0]
            val verticesB = vertices[indexes.second ?: 0]
            verticesA.point.let { widthA ->
                val pointA = sourceToViewCoordInt(widthA)

                verticesB.point.let { widthB ->
                    val labelPoint = Point(pointA.x, pointA.y)
                    moveLengthWidthLabelIfNeed(
                        canvas,
                        labelPoint,
                        indexes.first ?: 0,
                        letter,
                        index,
                        isLengthLabel,
                        listOf(verticesA, verticesB)
                    )
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private fun moveRectangleButtonLabelIfPossible(
        startIndex: Int,
        vertices: ArrayList<Vertices>,
        closeButton: CloseButton,
        number: Int,
        isAreaClearButton: Boolean
    ): com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition {

        val functionStartTime = System.currentTimeMillis()
        var methodStartTime: Long
        var endTime: Long
        var duration: Long

        Timber.tag("moveRectangleButtonLabelIfPossible func").d("start")

        val newPointIndex = if (startIndex < 0) {
            0
        } else {
            startIndex
        }

        val verticesPoint = sourceToViewCoordInt(vertices[newPointIndex].point)

        val verticesRectanglePointList = if (isAreaClearButton) {
            getAreaClearRectangleLabelPoints(verticesPoint, -1, closeButton, number)
        } else {
            getAreaRectangleLabelPoints(verticesPoint, -1, closeButton, number)
        }

        val newPoint = com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition(
            newPointIndex,
            0,
            Pair(0, 0),
            verticesRectanglePointList
        )

        var currentIndex = startIndex

        while (startIndex != currentIndex + 1) {
            if (currentIndex < vertices.size && currentIndex >= 0) {
                if (currentDraggedVertices?.point != vertices[currentIndex].point) {
                    val currentPoint = vertices[currentIndex].point
                    val currentPointOnView = sourceToViewCoordInt(currentPoint)

                    val rectanglePointList = if (isAreaClearButton) {
                        getAreaClearRectangleLabelPoints(
                            currentPointOnView,
                            -1,
                            closeButton,
                            number
                        )
                    } else {
                        getAreaRectangleLabelPoints(currentPointOnView, -1, closeButton, number)
                    }

                    methodStartTime = System.currentTimeMillis()
                    val isNewLabelPointCrossAnyOutlines =
                        isLabelCrossAnyOutlines(rectanglePointList)

                    endTime = System.currentTimeMillis()
                    duration = endTime - methodStartTime
                    Timber.tag("moveRectangleButtonLabelIfPossible")
                        .d("checkIfCloseLabelCrossNearAnyOutlines duration time = $duration ms")

                    methodStartTime = System.currentTimeMillis()
                    val isLabelTouchAnyClearLabels =
                        isAreaLabelTouchAnyAreaLabel(
                            rectanglePointList,
                            number - 1,
                            isAreaClearButton, closeButton, number
                        )
                    endTime = System.currentTimeMillis()
                    duration = endTime - methodStartTime
                    Timber.tag("moveRectangleButtonLabelIfPossible")
                        .d("isAreaLabelTouchAnyAreaLabel duration time = $duration ms")

                    val isCrossingAnyLengthWidthLabel =
                        if (lengthWidthLabelCoordinates.isNotEmpty()) {
                            methodStartTime = System.currentTimeMillis()
                            val isAreaLabelCrossing =
                                isAreaLabelCrossAnyOrientationLabels(rectanglePointList)
                            endTime = System.currentTimeMillis()
                            duration = endTime - methodStartTime
                            Timber.tag("moveRectangleButtonLabelIfPossible")
                                .d("isAreaLabelCrossAnyOrientationLabels duration time = $duration ms")
                            isAreaLabelCrossing
                        } else false

                    val isRectangleFitsInTheScreen =
                        rectanglePointList[0].x > 0 && rectanglePointList[1].x < width && rectanglePointList[0].y > 0 && rectanglePointList[3].y < height

                    if (!isNewLabelPointCrossAnyOutlines && !isLabelTouchAnyClearLabels && !isCrossingAnyLengthWidthLabel && currentPointOnView.x < width && currentPointOnView.x > 0 && currentPointOnView.y > 0 && currentPointOnView.y < height && isRectangleFitsInTheScreen) {

                        var minPoint = Pair(0, Int.MAX_VALUE)
                        vertices.forEachIndexed { verticesIndex, vertices ->
                            val verticesToView = sourceToViewCoordInt(vertices.point)
                            rectanglePointList.forEachIndexed { index, point ->
                                val distance =
                                    com.example.samplewoundsdk.utils.image.drawstroke.PolygonGeometry.calculateDistance(
                                        verticesToView,
                                        point
                                    ).toInt()
                                if (distance < minPoint.second) {
                                    minPoint = Pair(index, distance)
                                    currentIndex = verticesIndex
                                }
                            }
                        }

                        val verticesToView = sourceToViewCoordInt(vertices[currentIndex].point)

                        val pointXMoveDistance =
                            verticesToView.x - rectanglePointList[minPoint.first].x
                        val pointYMoveDistance =
                            verticesToView.y - rectanglePointList[minPoint.first].y

                        endTime = System.currentTimeMillis()
                        duration = endTime - functionStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible func")
                            .d("duration time = $duration")

                        return com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition(
                            outlinePointIndex = currentIndex,
                            rectanglePointIndex = minPoint.first,
                            drawDifference = Pair(pointXMoveDistance, pointYMoveDistance),
                            rectangle = rectanglePointList
                        )
                    }

                    var step = 1
                    val rectangleHeight = abs(rectanglePointList[0].y - rectanglePointList[3].y)
                    val availableHeight = rectangleHeight * 3

                    val availableWidth = abs(rectanglePointList[0].x - rectanglePointList[1].x) * 2
                    val topEdge = currentPointOnView.y - availableHeight
                    val bottomEdge = currentPointOnView.y + availableHeight
                    val leftEdge = currentPointOnView.x - availableWidth
                    val rightEdge = currentPointOnView.x + availableWidth

                    val moveStep = (rectangleHeight / 2f).toInt()

                    fun calculateCurrentStep(): Int {
                        return moveStep * step + rectangleHeight / 2
                    }

                    var currentStep = calculateCurrentStep()
                    while (currentPointOnView.y + currentStep < bottomEdge && currentPointOnView.y + currentStep < height && currentPointOnView.y + currentStep > 0 && currentPointOnView.x < width && currentPointOnView.x > 0) {
                        currentStep = calculateCurrentStep()

                        val newRectanglePointList = ArrayList(rectanglePointList.map {
                            Point(it.x, it.y + currentStep)
                        })

                        methodStartTime = System.currentTimeMillis()

                        val isNewPointCrossingAnyOutlines =
                            isLabelCrossAnyOutlines(newRectanglePointList)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isLabelCrossAnyOutlines duration time = $duration ms")

                        methodStartTime = System.currentTimeMillis()
                        val isLabelTouchAnyClearLabels =
                            isAreaLabelTouchAnyAreaLabel(
                                newRectanglePointList,
                                number - 1,
                                isAreaClearButton, closeButton, number
                            )

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isAreaLabelTouchAnyAreaLabel duration time = $duration ms")


                        val isCrossingAnyLengthWidthLabel =
                            if (lengthWidthLabelCoordinates.isNotEmpty()) {
                                methodStartTime = System.currentTimeMillis()
                                val isAreaLabelCrossing =
                                    isAreaLabelCrossAnyOrientationLabels(newRectanglePointList)

                                endTime = System.currentTimeMillis()
                                duration = endTime - methodStartTime
                                Timber.tag("moveRectangleButtonLabelIfPossible")
                                    .d("isAreaLabelCrossAnyOrientationLabels duration time = $duration ms")
                                isAreaLabelCrossing
                            } else false

                        val isRectangleFitsInTheScreen =
                            newRectanglePointList[0].x > 0 && newRectanglePointList[1].x < width && newRectanglePointList[0].y > 0 && newRectanglePointList[3].y < height

                        if (!isNewPointCrossingAnyOutlines && !isLabelTouchAnyClearLabels && !isCrossingAnyLengthWidthLabel && isRectangleFitsInTheScreen) {
                            var minPoint = Pair(0, Int.MAX_VALUE)
                            vertices.forEachIndexed { verticesIndex, vertices ->
                                val verticesToView = sourceToViewCoordInt(vertices.point)
                                newRectanglePointList.forEachIndexed { index, point ->
                                    val distance =
                                        com.example.samplewoundsdk.utils.image.drawstroke.PolygonGeometry.calculateDistance(
                                            verticesToView,
                                            point
                                        ).toInt()
                                    if (distance < minPoint.second) {
                                        minPoint = Pair(index, distance)
                                        currentIndex = verticesIndex
                                    }
                                }
                            }

                            val verticesToView = sourceToViewCoordInt(vertices[currentIndex].point)

                            val pointXMoveDistance =
                                verticesToView.x - newRectanglePointList[minPoint.first].x
                            val pointYMoveDistance =
                                verticesToView.y - newRectanglePointList[minPoint.first].y

                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveRectangleButtonLabelIfPossible func")
                                .d("duration time = $duration")

                            return com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition(
                                outlinePointIndex = currentIndex,
                                rectanglePointIndex = minPoint.first,
                                drawDifference = Pair(pointXMoveDistance, pointYMoveDistance),
                                rectangle = newRectanglePointList
                            )
                        }
                        step++
                    }
                    step = 1
                    currentStep = calculateCurrentStep()
                    while (currentPointOnView.y - currentStep > topEdge && currentPointOnView.y - currentStep > 0 && currentPointOnView.y - currentStep < height && currentPointOnView.x < width && currentPointOnView.x > 0) {
                        currentStep = calculateCurrentStep()


                        val newRectanglePointList = ArrayList(rectanglePointList.map {
                            Point(it.x, it.y - currentStep)
                        })
                        methodStartTime = System.currentTimeMillis()

                        val isNewLabelPointCrossAnyOutlines =
                            isLabelCrossAnyOutlines(newRectanglePointList)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isLabelCrossAnyOutlines duration time = $duration ms")

                        methodStartTime = System.currentTimeMillis()
                        val isLabelTouchAnyClearLabels =
                            isAreaLabelTouchAnyAreaLabel(
                                newRectanglePointList,
                                number - 1,
                                isAreaClearButton, closeButton, number
                            )

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isAreaLabelTouchAnyAreaLabel duration time = $duration ms")


                        val isCrossingAnyLengthWidthLabel =
                            if (lengthWidthLabelCoordinates.isNotEmpty()) {
                                methodStartTime = System.currentTimeMillis()
                                val isAreaLabelCrossing =
                                    isAreaLabelCrossAnyOrientationLabels(newRectanglePointList)

                                endTime = System.currentTimeMillis()
                                duration = endTime - methodStartTime
                                Timber.tag("moveRectangleButtonLabelIfPossible")
                                    .d("isAreaLabelCrossAnyOrientationLabels duration time = $duration ms")
                                isAreaLabelCrossing
                            } else false

                        val isRectangleFitsInTheScreen =
                            newRectanglePointList[0].x > 0 && newRectanglePointList[1].x < width && newRectanglePointList[0].y > 0 && newRectanglePointList[3].y < height

                        if (!isNewLabelPointCrossAnyOutlines && !isLabelTouchAnyClearLabels && !isCrossingAnyLengthWidthLabel && isRectangleFitsInTheScreen) {
                            var minPoint = Pair(0, Int.MAX_VALUE)
                            vertices.forEachIndexed { verticesIndex, vertices ->
                                val verticesToView = sourceToViewCoordInt(vertices.point)
                                newRectanglePointList.forEachIndexed { index, point ->
                                    val distance =
                                        com.example.samplewoundsdk.utils.image.drawstroke.PolygonGeometry.calculateDistance(
                                            verticesToView,
                                            point
                                        ).toInt()
                                    if (distance < minPoint.second) {
                                        minPoint = Pair(index, distance)
                                        currentIndex = verticesIndex
                                    }
                                }
                            }

                            val verticesToView = sourceToViewCoordInt(vertices[currentIndex].point)

                            val pointXMoveDistance =
                                verticesToView.x - newRectanglePointList[minPoint.first].x
                            val pointYMoveDistance =
                                verticesToView.y - newRectanglePointList[minPoint.first].y

                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveRectangleButtonLabelIfPossible func")
                                .d("duration time = $duration")

                            return com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition(
                                outlinePointIndex = currentIndex,
                                rectanglePointIndex = minPoint.first,
                                drawDifference = Pair(pointXMoveDistance, pointYMoveDistance),
                                rectangle = newRectanglePointList
                            )
                        }
                        step++
                    }
                    step = 1
                    currentStep = calculateCurrentStep()
                    while (currentPointOnView.x + currentStep < rightEdge && currentPointOnView.x + currentStep < width && currentPointOnView.x + currentStep > 0 && currentPointOnView.y < height && currentPointOnView.y > 0) {
                        currentStep = calculateCurrentStep()

                        val newRectanglePointList = ArrayList(rectanglePointList.map {
                            Point(it.x + currentStep, it.y)
                        })
                        methodStartTime = System.currentTimeMillis()

                        val isNewLabelPointCrossAnyOutlines =
                            isLabelCrossAnyOutlines(newRectanglePointList)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isLabelCrossAnyOutlines duration time = $duration ms")

                        methodStartTime = System.currentTimeMillis()
                        val isLabelTouchAnyClearLabels =
                            isAreaLabelTouchAnyAreaLabel(
                                newRectanglePointList,
                                number - 1,
                                isAreaClearButton, closeButton, number
                            )

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isAreaLabelTouchAnyAreaLabel duration time = $duration ms")


                        val isCrossingAnyLengthWidthLabel =
                            if (lengthWidthLabelCoordinates.isNotEmpty()) {
                                methodStartTime = System.currentTimeMillis()
                                val isAreaLabelCrossing =
                                    isAreaLabelCrossAnyOrientationLabels(newRectanglePointList)

                                endTime = System.currentTimeMillis()
                                duration = endTime - methodStartTime
                                Timber.tag("drawAreaLabels")
                                    .d("isAreaLabelCrossAnyOrientationLabels duration time = $duration ms")
                                isAreaLabelCrossing
                            } else false

                        val isRectangleFitsInTheScreen =
                            newRectanglePointList[0].x > 0 && newRectanglePointList[1].x < width && newRectanglePointList[0].y > 0 && newRectanglePointList[3].y < height

                        if (!isNewLabelPointCrossAnyOutlines && !isLabelTouchAnyClearLabels && !isCrossingAnyLengthWidthLabel && isRectangleFitsInTheScreen) {
                            var minPoint = Pair(0, Int.MAX_VALUE)
                            vertices.forEachIndexed { verticesIndex, vertices ->
                                val verticesToView = sourceToViewCoordInt(vertices.point)
                                newRectanglePointList.forEachIndexed { index, point ->
                                    val distance =
                                        com.example.samplewoundsdk.utils.image.drawstroke.PolygonGeometry.calculateDistance(
                                            verticesToView,
                                            point
                                        ).toInt()
                                    if (distance < minPoint.second) {
                                        minPoint = Pair(index, distance)
                                        currentIndex = verticesIndex
                                    }
                                }
                            }

                            val verticesToView = sourceToViewCoordInt(vertices[currentIndex].point)

                            val pointXMoveDistance =
                                verticesToView.x - newRectanglePointList[minPoint.first].x
                            val pointYMoveDistance =
                                verticesToView.y - newRectanglePointList[minPoint.first].y

                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveRectangleButtonLabelIfPossible func")
                                .d("duration time = $duration")

                            return com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition(
                                outlinePointIndex = currentIndex,
                                rectanglePointIndex = minPoint.first,
                                drawDifference = Pair(pointXMoveDistance, pointYMoveDistance),
                                rectangle = newRectanglePointList
                            )
                        }
                        step++
                    }
                    step = 1
                    currentStep = calculateCurrentStep()
                    while (currentPointOnView.x - currentStep > leftEdge && currentPointOnView.x - currentStep > 0 && currentPointOnView.x - currentStep < width && currentPointOnView.y < height && currentPointOnView.y > 0) {
                        currentStep = calculateCurrentStep()

                        val newRectanglePointList = ArrayList(rectanglePointList.map {
                            Point(it.x - currentStep, it.y)
                        })

                        val isNewLabelPointCrossAnyOutlines =
                            isLabelCrossAnyOutlines(newRectanglePointList)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isLabelCrossAnyOutlines duration time = $duration ms")

                        methodStartTime = System.currentTimeMillis()
                        val isLabelTouchAnyClearLabels =
                            isAreaLabelTouchAnyAreaLabel(
                                newRectanglePointList,
                                number - 1,
                                isAreaClearButton, closeButton, number
                            )

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveRectangleButtonLabelIfPossible")
                            .d("isAreaLabelTouchAnyAreaLabel duration time = $duration ms")


                        val isCrossingAnyLengthWidthLabel =
                            if (lengthWidthLabelCoordinates.isNotEmpty()) {
                                methodStartTime = System.currentTimeMillis()
                                val isAreaLabelCrossing =
                                    isAreaLabelCrossAnyOrientationLabels(newRectanglePointList)

                                endTime = System.currentTimeMillis()
                                duration = endTime - methodStartTime
                                Timber.tag("drawAreaLabels")
                                    .d("isAreaLabelCrossAnyOrientationLabels duration time = $duration ms")
                                isAreaLabelCrossing
                            } else false

                        val isRectangleFitsInTheScreen =
                            newRectanglePointList[0].x > 0 && newRectanglePointList[1].x < width && newRectanglePointList[0].y > 0 && newRectanglePointList[3].y < height

                        if (!isNewLabelPointCrossAnyOutlines && !isLabelTouchAnyClearLabels && !isCrossingAnyLengthWidthLabel && isRectangleFitsInTheScreen) {
                            var minPoint = Pair(0, Int.MAX_VALUE)
                            vertices.forEachIndexed { verticesIndex, vertices ->
                                val verticesToView = sourceToViewCoordInt(vertices.point)
                                newRectanglePointList.forEachIndexed { index, point ->
                                    val distance =
                                        com.example.samplewoundsdk.utils.image.drawstroke.PolygonGeometry.calculateDistance(
                                            verticesToView,
                                            point
                                        ).toInt()
                                    if (distance < minPoint.second) {
                                        minPoint = Pair(index, distance)
                                        currentIndex = verticesIndex
                                    }
                                }
                            }

                            val verticesToView = sourceToViewCoordInt(vertices[currentIndex].point)

                            val pointXMoveDistance =
                                verticesToView.x - newRectanglePointList[minPoint.first].x
                            val pointYMoveDistance =
                                verticesToView.y - newRectanglePointList[minPoint.first].y

                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveRectangleButtonLabelIfPossible func")
                                .d("duration time = $duration")

                            return com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition(
                                outlinePointIndex = currentIndex,
                                rectanglePointIndex = minPoint.first,
                                drawDifference = Pair(pointXMoveDistance, pointYMoveDistance),
                                rectangle = newRectanglePointList
                            )
                        }
                        step++
                    }
                }
                if (currentIndex + 1 < vertices.size) {
                    currentIndex++
                } else if (currentIndex + 1 == vertices.size && startIndex == 0) {
                    currentIndex = -1
                } else {
                    currentIndex = 0
                }
            } else {
                endTime = System.currentTimeMillis()
                duration = endTime - functionStartTime
                Timber.tag("moveRectangleButtonLabelIfPossible func").d("duration time = $duration")
                return newPoint
            }
        }
        val firstPoint = sourceToViewCoordInt(vertices[0].point)

        val rectanglePointList = if (isAreaClearButton) {
            getAreaClearRectangleLabelPoints(firstPoint, -1, closeButton, number)
        } else {
            getAreaRectangleLabelPoints(firstPoint, -1, closeButton, number)
        }

        endTime = System.currentTimeMillis()
        duration = endTime - functionStartTime
        Timber.tag("moveRectangleButtonLabelIfPossible func").d("duration time = $duration")
        return com.example.samplewoundsdk.data.pojo.measurement.RectanglePosition(
            0,
            0,
            Pair(0, 0),
            rectanglePointList
        )
    }

    private fun moveOrientationLabel(
        startIndex: Int,
        allowedDistance: Float,
        vertices: List<Vertices>,
        labelsIndex: Int
    ): com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition {
        val functionStartTime = System.currentTimeMillis()
        var methodStartTime: Long
        var endTime: Long
        var duration: Long
        Timber.tag("moveOrientationLabel func").d("start")
        vertices.forEachIndexed { currentIndex, vertix ->
            if (currentIndex < vertices.size) {
                if (currentDraggedVertices?.point != vertices[currentIndex].point) {
                    val currentPoint = vertices[currentIndex].point
                    val outlineIndex =
                        verticesList[labelsIndex].indexOfFirst { it.point == currentPoint }
                    val currentPointOnView = sourceToViewCoordInt(currentPoint)

                    var step = 1
                    val availableHeight = allowedDistance * 3f
                    val availableWidth = allowedDistance * 3f

                    val topEdge = currentPointOnView.y - availableHeight
                    val bottomEdge = currentPointOnView.y + availableHeight
                    val leftEdge = currentPointOnView.x - availableWidth
                    val rightEdge = currentPointOnView.x + availableWidth
                    val moveStep = (allowedDistance / 2f).toInt()

                    fun calculateCurrentStep(): Int {
                        return moveStep * step + allowedDistance.toInt()
                    }

                    var currentStep = calculateCurrentStep()
                    while (currentPointOnView.y + currentStep < bottomEdge && currentPointOnView.y + currentStep < height && currentPointOnView.y + currentStep > 0 && currentPointOnView.x < width && currentPointOnView.x > 0) {
                        currentStep = calculateCurrentStep()

                        val newPoint =
                            Point(currentPointOnView.x, currentPointOnView.y + currentStep)

                        methodStartTime = System.currentTimeMillis()

                        val isOrientationLabelTouchAnyOrientationLabels =
                            isOrientationLabelCrossAnyOrientationLabel(newPoint, labelsIndex)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isOrientationLabelCrossAnyOrientationLabel duration time = $duration ms")

                        methodStartTime = System.currentTimeMillis()

                        val isNewLabelInsideAnyOutlines =
                            isLengthWidthLabelInsideAnyOutlines(newPoint, allowedDistance)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isLengthWidthLabelInsideAnyOutlines duration time = $duration ms")

                        if (!isOrientationLabelTouchAnyOrientationLabels.first && !isNewLabelInsideAnyOutlines) {
                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveOrientationLabel func")
                                .d("duration time = $duration ms")

                            val pointXMoveDistance =
                                currentPointOnView.x - newPoint.x
                            val pointYMoveDistance =
                                currentPointOnView.y - newPoint.y

                            return com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition(
                                outlineIndex,
                                Pair(pointXMoveDistance, pointYMoveDistance),
                                newPoint
                            )
                        }
                        step++
                    }
                    step = 1
                    currentStep = calculateCurrentStep()
                    while (currentPointOnView.y - currentStep > topEdge && currentPointOnView.y - currentStep > 0 && currentPointOnView.y - currentStep < height && currentPointOnView.x < width && currentPointOnView.x > 0) {
                        currentStep = calculateCurrentStep()
                        val newPoint =
                            Point(currentPointOnView.x, currentPointOnView.y - currentStep)

                        methodStartTime = System.currentTimeMillis()

                        val isOrientationLabelTouchAnyOrientationLabels =
                            isOrientationLabelCrossAnyOrientationLabel(newPoint, labelsIndex)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isOrientationLabelCrossAnyOrientationLabel duration time = $duration ms")


                        methodStartTime = System.currentTimeMillis()

                        val isNewLabelInsideAnyOutlines =
                            isLengthWidthLabelInsideAnyOutlines(newPoint, allowedDistance)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isLengthWidthLabelInsideAnyOutlines duration time = $duration ms")

                        if (!isOrientationLabelTouchAnyOrientationLabels.first && !isNewLabelInsideAnyOutlines) {
                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveOrientationLabel func")
                                .d("duration time = $duration ms")
                            val pointXMoveDistance =
                                currentPointOnView.x - newPoint.x
                            val pointYMoveDistance =
                                currentPointOnView.y - newPoint.y

                            return com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition(
                                outlineIndex,
                                Pair(pointXMoveDistance, pointYMoveDistance),
                                newPoint
                            )
                        }
                        step++
                    }
                    step = 1
                    currentStep = calculateCurrentStep()
                    while (currentPointOnView.x + currentStep < rightEdge && currentPointOnView.x + currentStep < width && currentPointOnView.x + currentStep > 0 && currentPointOnView.y < height && currentPointOnView.y > 0) {
                        currentStep = calculateCurrentStep()
                        val newPoint =
                            Point(currentPointOnView.x + currentStep, currentPointOnView.y)

                        methodStartTime = System.currentTimeMillis()

                        val isOrientationLabelTouchAnyOrientationLabels =
                            isOrientationLabelCrossAnyOrientationLabel(newPoint, labelsIndex)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isOrientationLabelCrossAnyOrientationLabel duration time = $duration ms")


                        methodStartTime = System.currentTimeMillis()

                        val isNewLabelInsideAnyOutlines =
                            isLengthWidthLabelInsideAnyOutlines(newPoint, allowedDistance)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isLengthWidthLabelInsideAnyOutlines duration time = $duration ms")

                        if (!isOrientationLabelTouchAnyOrientationLabels.first && !isNewLabelInsideAnyOutlines) {
                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveOrientationLabel func")
                                .d("duration time = $duration ms")
                            val pointXMoveDistance =
                                currentPointOnView.x - newPoint.x
                            val pointYMoveDistance =
                                currentPointOnView.y - newPoint.y

                            return com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition(
                                outlineIndex,
                                Pair(pointXMoveDistance, pointYMoveDistance),
                                newPoint
                            )
                        }
                        step++
                    }
                    step = 1
                    currentStep = calculateCurrentStep()
                    while (currentPointOnView.x - currentStep > leftEdge && currentPointOnView.x - currentStep > 0 && currentPointOnView.x - currentStep < width && currentPointOnView.y < height && currentPointOnView.y > 0) {
                        currentStep = calculateCurrentStep()
                        val newPoint =
                            Point(currentPointOnView.x - currentStep, currentPointOnView.y)

                        methodStartTime = System.currentTimeMillis()

                        val isOrientationLabelTouchAnyOrientationLabels =
                            isOrientationLabelCrossAnyOrientationLabel(newPoint, labelsIndex)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isOrientationLabelCrossAnyOrientationLabel duration time = $duration ms")


                        methodStartTime = System.currentTimeMillis()

                        val isNewLabelInsideAnyOutlines =
                            isLengthWidthLabelInsideAnyOutlines(newPoint, allowedDistance)

                        endTime = System.currentTimeMillis()
                        duration = endTime - methodStartTime
                        Timber.tag("moveOrientationLabel")
                            .d("isLengthWidthLabelInsideAnyOutlines duration time = $duration ms")

                        if (!isOrientationLabelTouchAnyOrientationLabels.first && !isNewLabelInsideAnyOutlines) {
                            endTime = System.currentTimeMillis()
                            duration = endTime - functionStartTime
                            Timber.tag("moveOrientationLabel func")
                                .d("duration time = $duration ms")
                            val pointXMoveDistance =
                                currentPointOnView.x - newPoint.x
                            val pointYMoveDistance =
                                currentPointOnView.y - newPoint.y

                            return com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition(
                                outlineIndex,
                                Pair(pointXMoveDistance, pointYMoveDistance),
                                newPoint
                            )
                        }
                        step++
                    }
                }
            } else {
                endTime = System.currentTimeMillis()
                duration = endTime - functionStartTime
                Timber.tag("moveOrientationLabel func").d("duration time = $duration ms")
                return com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition(
                    startIndex,
                    Pair(0, 0),
                    vertices[startIndex].point
                )
            }
        }
        endTime = System.currentTimeMillis()
        duration = endTime - functionStartTime
        Timber.tag("moveOrientationLabel func").d("duration time = $duration ms")
        return com.example.samplewoundsdk.data.pojo.measurement.LengthWidthButtonPosition(
            startIndex,
            Pair(0, 0),
            vertices[startIndex].point
        )
    }

    private fun isLabelCrossAnyOutlines(
        rectanglePointList: ArrayList<Point>
    ): Boolean {

        val labelEdgePoints = ArrayList<Pair<Point, Point>>(rectanglePointList.map {
            Pair(Point(it.x, -height * 4), it)
        })

        val sidePointList = ArrayList<Pair<Point, Point>>()
        sidePointList.add(Pair(rectanglePointList[0], rectanglePointList[1]))
        sidePointList.add(Pair(rectanglePointList[1], rectanglePointList[2]))
        sidePointList.add(Pair(rectanglePointList[2], rectanglePointList[3]))
        sidePointList.add(Pair(rectanglePointList[3], rectanglePointList[0]))

        return checkIntersect(labelEdgePoints, sidePointList)
    }

    private fun isLengthWidthLabelInsideAnyOutlines(
        currentPoint: Point,
        allowedDistance: Float
    ): Boolean {

        val labelLeftPoint = Point(currentPoint.x - allowedDistance.toInt(), currentPoint.y)
        val labelRightPoint = Point(currentPoint.x + allowedDistance.toInt(), currentPoint.y)
        val labelTopPoint = Point(currentPoint.x, currentPoint.y - allowedDistance.toInt())
        val labelBottomPoint = Point(currentPoint.x, currentPoint.y + allowedDistance.toInt())

        val labelEdgePoints = ArrayList<Pair<Point, Point>>()
        labelEdgePoints.add(Pair(Point(labelLeftPoint.x, -height * 4), labelLeftPoint))
        labelEdgePoints.add(Pair(Point(labelRightPoint.x, -height * 4), labelRightPoint))
        labelEdgePoints.add(Pair(Point(labelTopPoint.x, -height * 4), labelTopPoint))
        labelEdgePoints.add(Pair(Point(labelBottomPoint.x, -height * 4), labelBottomPoint))

        return checkIntersect(labelEdgePoints)
    }

    private fun checkIntersect(
        labelEdgePoints: ArrayList<Pair<Point, Point>>,
        sidePointList: ArrayList<Pair<Point, Point>> = ArrayList()
    ): Boolean {
        sidePointList.forEach {
            verticesList.forEach { vertices ->
                for (i in 0 until vertices.size step 1) {
                    val pointA = sourceToViewCoordInt(vertices[i].point)
                    val pointB = if (i + 1 > vertices.lastIndex) {
                        var index = if (i + 1 == vertices.size) {
                            0
                        } else {
                            abs(vertices.size - 1 - i)
                        }

                        while (index > vertices.lastIndex) {
                            index -= 1
                        }

                        sourceToViewCoordInt(vertices[index].point)
                    } else {
                        sourceToViewCoordInt(vertices[i + 1].point)
                    }

                    if (linesIntersect(pointA, pointB, it.second, it.first)) {
                        return true
                    }
                }
            }
        }
        labelEdgePoints.forEach {
            var countIntersectObjects = 0
            verticesList.forEach { vertices ->
                val crossPoints = ArrayList<Point>()
                for (i in 0 until vertices.size step 1) {
                    val pointA = sourceToViewCoordInt(vertices[i].point)
                    val pointB = if (i + 1 > vertices.lastIndex) {
                        var index = if (i + 1 == vertices.size) {
                            0
                        } else {
                            abs(vertices.size - 1 - i)
                        }

                        while (index > vertices.lastIndex) {
                            index -= 1
                        }

                        sourceToViewCoordInt(vertices[index].point)
                    } else {
                        sourceToViewCoordInt(vertices[i + 1].point)
                    }

                    if (linesIntersect(pointA, pointB, it.second, it.first)) {
                        var alreadyCounted = false
                        if (it.second.x == pointA.x || it.second.y == pointA.y) {
                            if (crossPoints.contains(pointA)) {
                                alreadyCounted = true
                            } else {
                                crossPoints.add(pointA)
                            }
                        }
                        if (it.second.x == pointB.x || it.second.y == pointA.y) {
                            if (crossPoints.contains(pointB)) {
                                alreadyCounted = true
                            } else {
                                crossPoints.add(pointB)
                            }
                        }
                        if (!alreadyCounted) {
                            countIntersectObjects++
                        }
                    }
                }
            }
            if (countIntersectObjects % 2 != 0) {
                return true
            }
        }
        return false
    }


    fun orientation(
        p: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint,
        q: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint,
        r: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint
    ): Int {
        val value = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
        return when {
            value == 0.0 -> 0 // collinear
            value > 0 -> 1   // clockwise
            else -> 2        // anticlockwise
        }
    }

    private fun onSegment(
        p: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint,
        q: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint,
        r: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint
    ): Boolean {
        return q.x <= max(p.x, r.x) && q.x >= min(p.x, r.x) &&
                q.y <= max(p.y, r.y) && q.y >= min(p.y, r.y)
    }

    private fun doIntersect(
        p1: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint,
        q1: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint,
        p2: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint,
        q2: com.example.samplewoundsdk.data.pojo.measurement.IntersectPoint
    ): Boolean {
        val o1 = orientation(p1, q1, p2)
        val o2 = orientation(p1, q1, q2)
        val o3 = orientation(p2, q2, p1)
        val o4 = orientation(p2, q2, q1)

        if (o1 != o2 && o3 != o4) {
            return true
        }

        if (o1 == 0 && onSegment(p1, p2, q1)) return true
        if (o2 == 0 && onSegment(p1, q2, q1)) return true
        if (o3 == 0 && onSegment(p2, p1, q2)) return true
        if (o4 == 0 && onSegment(p2, q1, q2)) return true

        return false
    }

    private fun getAreaRectangleLabelPoints(
        center: Point,
        rectanglePointIndex: Int,
        closeButton: CloseButton,
        number: Int
    ): ArrayList<Point> {
        return closeButton.getAreaRectangleByPoint(center, rectanglePointIndex, this, number)
    }

    private fun getAreaClearRectangleLabelPoints(
        center: Point,
        rectanglePointIndex: Int,
        closeButton: CloseButton,
        number: Int
    ): ArrayList<Point> {
        return closeButton.getAreaClearRectangleByPoint(center, rectanglePointIndex, this, number)
    }

    private fun drawLabelCircle(
        canvas: Canvas,
        labelPoint: Point,
        letter: String
    ) {
        canvas.drawCircle(
            labelPoint.x.toFloat(),
            labelPoint.y.toFloat(),
            RADIUS * 2f,
            linesPaint!!
        )
        val bounds = Rect()
        textPaint!!.getTextBounds(letter, 0, letter.length, bounds)
        val labelPointX = labelPoint.x - bounds.width() / 2f
        val labelPointY = labelPoint.y + bounds.height() / 2f
        canvas.drawText(
            letter,
            labelPointX,
            labelPointY,
            textPaint!!
        )
    }

    private fun isOrientationLabelCrossAnyOrientationLabel(
        position: Point,
        index: Int
    ): Pair<Boolean, Point?> {
        val allowedDistance = RADIUS * 2f
        val firstRectangleEdges =
            com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromCircle(
                position,
                allowedDistance
            )

        lengthWidthLabelCoordinates.forEachIndexed { labelIndex, lengthWidthLabelPoint ->
            if (labelIndex != index) {
                if (lengthWidthLabelPoint.first != null) {
                    val lengthOutlinePoint =
                        sourceToViewCoordInt(verticesList[labelIndex][lengthWidthLabelPoint.first?.outlinePointIndex!!].point)

                    val xDifference =
                        lengthOutlinePoint.x - (lengthWidthLabelPoint.first?.drawDifference?.first!!)
                    val yDifference =
                        lengthOutlinePoint.y - (lengthWidthLabelPoint.first?.drawDifference?.second!!)

                    val lengthLabelPointToView = Point(xDifference, yDifference)

                    val secondRectangleEdges =
                        com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromCircle(
                            lengthLabelPointToView,
                            allowedDistance
                        )
                    if (checkIfLabelTouchOrientationCircle(
                            firstRectangleEdges,
                            secondRectangleEdges
                        ) || checkIfLabelTouchOrientationCircle(
                            secondRectangleEdges,
                            firstRectangleEdges
                        )
                    ) {
                        return Pair(true, lengthLabelPointToView)
                    }
                }
                if (lengthWidthLabelPoint.second != null) {
                    val widthOutlinePoint =
                        sourceToViewCoordInt(verticesList[labelIndex][lengthWidthLabelPoint.second?.outlinePointIndex!!].point)

                    val xDifference =
                        widthOutlinePoint.x - (lengthWidthLabelPoint.second?.drawDifference?.first!!)
                    val yDifference =
                        widthOutlinePoint.y - (lengthWidthLabelPoint.second?.drawDifference?.second!!)

                    val widthLabelPointToView = Point(xDifference, yDifference)

                    val secondRectangleEdges =
                        com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromCircle(
                            widthLabelPointToView,
                            allowedDistance
                        )

                    if (
                        checkIfLabelTouchOrientationCircle(
                            firstRectangleEdges,
                            secondRectangleEdges
                        ) ||
                        checkIfLabelTouchOrientationCircle(
                            secondRectangleEdges,
                            firstRectangleEdges
                        )
                    ) {
                        return Pair(true, widthLabelPointToView)
                    }
                }
            }
        }
        return Pair(false, null)
    }

    private fun isAreaLabelTouchAnyAreaLabel(
        rectangleLabelPointList: ArrayList<Point>,
        rectangleIndex: Int,
        isAreaClearButton: Boolean,
        closeButton: CloseButton,
        number: Int
    ): Boolean {

        val firstRectangleEdges =
            com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromRectangle(
                rectangleLabelPointList
            )

        clearButtonRectangleCoordinates.forEachIndexed { index, rectanglePoints ->
            if (index != rectangleIndex) {
                val pointIndex = clearButtonLabelCoordinates[index].areaIndex
                val currentOutlinePoint =
                    sourceToViewCoordInt(verticesList[index][pointIndex].point)

                val pointXMoveDistance = currentOutlinePoint.x - rectanglePoints.second.first
                val pointYMoveDistance = currentOutlinePoint.y - rectanglePoints.second.second

                val rectanglePoint = Point(pointXMoveDistance, pointYMoveDistance)

                val rectangleCenter = if (isAreaClearButton) {
                    getAreaClearRectangleLabelPoints(
                        rectanglePoint,
                        rectanglePoints.first,
                        closeButton,
                        number
                    )
                } else {
                    getAreaRectangleLabelPoints(
                        rectanglePoint,
                        rectanglePoints.first,
                        closeButton,
                        number
                    )
                }

                val secondRectangleEdges =
                    com.example.samplewoundsdk.data.pojo.measurement.RectangleEdges.createFromRectangle(
                        rectangleCenter
                    )
                if (
                    checkIfLabelTouchOrientationCircle(firstRectangleEdges, secondRectangleEdges) ||
                    checkIfLabelTouchOrientationCircle(secondRectangleEdges, firstRectangleEdges)
                ) {
                    return true
                }
            }
        }
        return false
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

    private fun drawAreaLabels(
        index: Int,
        canvas: Canvas,
        vertices: ArrayList<Vertices>,
        isClearAreaButton: Boolean
    ) {
        val functionStartTime = System.currentTimeMillis()
        var methodStartTime: Long
        var endTime: Long
        var duration: Long

        Timber.tag("drawAreaLabels func").d("start")

        val isDrawPointCrossingAnyOutline: Boolean
        var startPoint: Point
        val startIndex: Int
        val bestPoint: Point
        var isPointValid = false

        startIndex = if (clearButtonLabelCoordinates.isNotEmpty()) {
            if (index <= clearButtonLabelCoordinates.lastIndex) {
                val clearButtonIndex = clearButtonLabelCoordinates[index].areaIndex
                isPointValid = clearButtonLabelCoordinates[index].isAreaValid
                startPoint = sourceToViewCoordInt(vertices[clearButtonIndex].point)
                clearButtonIndex
            } else {
                methodStartTime = System.currentTimeMillis()
                bestPoint =
                    closeButton[index].findBestOptionPointToDrawCloseButton( // find best label option
                        index + 1,
                        vertices,
                        this
                    )
                endTime = System.currentTimeMillis()
                duration = endTime - methodStartTime
                Timber.tag("drawAreaLabels")
                    .d("findBestOptionPointToDrawCloseButton duration time = $duration ms")
                startPoint = sourceToViewCoordInt(bestPoint)
                vertices.indexOfFirst { it.point == bestPoint }
            }
        } else {
            methodStartTime = System.currentTimeMillis()
            bestPoint =
                closeButton[index].findBestOptionPointToDrawCloseButton( // find best label option
                    index + 1,
                    vertices,
                    this
                )
            endTime = System.currentTimeMillis()
            duration = endTime - methodStartTime
            Timber.tag("drawAreaLabels")
                .d("findBestOptionPointToDrawCloseButton duration time = $duration ms")
            startPoint = sourceToViewCoordInt(bestPoint)
            vertices.indexOfFirst { it.point == bestPoint }
        }

        if (clearButtonRectangleCoordinates.isNotEmpty()) {
            if (index <= clearButtonRectangleCoordinates.lastIndex) {
                startPoint = Point(
                    startPoint.x - clearButtonRectangleCoordinates[index].second.first,
                    startPoint.y - clearButtonRectangleCoordinates[index].second.second
                )
            }
        }

        val rectanglePointList = if (isClearAreaButton) {
            getAreaClearRectangleLabelPoints(
                startPoint,
                clearButtonRectangleCoordinates.getOrNull(index)?.first ?: -1,
                closeButton[index],
                index + 1
            )
        } else {
            getAreaRectangleLabelPoints(
                startPoint,
                clearButtonRectangleCoordinates.getOrNull(index)?.first ?: -1,
                closeButton[index],
                index + 1

            )
        }

        if (isPointValid) {
            drawAreaLabel(isClearAreaButton, index, canvas, rectanglePointList)
        } else {
            methodStartTime = System.currentTimeMillis()
            val isLabelTouchAnyClearLabels =
                isAreaLabelTouchAnyAreaLabel(
                    rectanglePointList,
                    index,
                    isClearAreaButton,
                    closeButton[index],
                    index + 1
                )

            endTime = System.currentTimeMillis()
            duration = endTime - methodStartTime
            Timber.tag("drawAreaLabels")
                .d("isAreaLabelTouchAnyAreaLabel duration time = $duration ms")


            methodStartTime = System.currentTimeMillis()
            isDrawPointCrossingAnyOutline = isLabelCrossAnyOutlines(rectanglePointList)

            endTime = System.currentTimeMillis()
            duration = endTime - methodStartTime
            Timber.tag("drawAreaLabels")
                .d("checkIfCloseLabelCrossNearAnyOutlines duration time = $duration ms")

            val isCrossingAnyLengthWidthLabel =
                if (lengthWidthLabelCoordinates.isNotEmpty()) {
                    methodStartTime = System.currentTimeMillis()
                    val isCrossing = isAreaLabelCrossAnyOrientationLabels(rectanglePointList)
                    endTime = System.currentTimeMillis()
                    duration = endTime - methodStartTime
                    Timber.tag("drawAreaLabels")
                        .d("isAreaLabelCrossAnyOrientationLabels duration time = $duration ms")
                    isCrossing
                } else false

            val isRectangleFitsInTheScreen =
                rectanglePointList[0].x > 0 && rectanglePointList[1].x < width && rectanglePointList[0].y > 0 && rectanglePointList[3].y < height

            if (clearButtonLabelCoordinates.size > 1) {
                if (isDrawPointCrossingAnyOutline || isLabelTouchAnyClearLabels || isCrossingAnyLengthWidthLabel || !isRectangleFitsInTheScreen) {
                    val newLabelPoint = moveRectangleButtonLabelIfPossible(
                        startIndex,
                        vertices,
                        closeButton[index],
                        index + 1,
                        isAreaClearButton = isClearAreaButton
                    )

                    if (index > clearButtonLabelCoordinates.lastIndex) {
                        clearButtonLabelCoordinates.add(
                            AreaButton(
                                newLabelPoint.outlinePointIndex,
                                true
                            )
                        )
                    } else {
                        clearButtonLabelCoordinates[index] =
                            AreaButton(
                                newLabelPoint.outlinePointIndex,
                                true
                            )
                    }

                    drawAreaLabel(isClearAreaButton, index, canvas, newLabelPoint.rectangle)

                    if (clearButtonRectangleCoordinates.isEmpty()) {
                        clearButtonRectangleCoordinates.add(
                            Pair(
                                newLabelPoint.rectanglePointIndex,
                                newLabelPoint.drawDifference
                            )
                        )
                    } else {
                        if (index > clearButtonRectangleCoordinates.lastIndex) {
                            clearButtonRectangleCoordinates.add(
                                Pair(
                                    newLabelPoint.rectanglePointIndex,
                                    newLabelPoint.drawDifference
                                )
                            )
                        } else {
                            clearButtonRectangleCoordinates[index] =
                                Pair(
                                    newLabelPoint.rectanglePointIndex,
                                    newLabelPoint.drawDifference
                                )
                        }
                    }
                } else {
                    if (index > clearButtonLabelCoordinates.lastIndex) {
                        clearButtonLabelCoordinates.add(
                            AreaButton(
                                startIndex,
                                true
                            )
                        )
                    }
                    drawAreaLabel(isClearAreaButton, index, canvas, rectanglePointList)
                }
            } else {
                if (!isDrawPointCrossingAnyOutline && !isLabelTouchAnyClearLabels && !isCrossingAnyLengthWidthLabel && isRectangleFitsInTheScreen) {
                    if (clearButtonLabelCoordinates.isNotEmpty()) {
                        if (index > clearButtonLabelCoordinates.lastIndex) {
                            clearButtonLabelCoordinates.add(
                                AreaButton(
                                    startIndex,
                                    true
                                )
                            )
                        }
                    } else {
                        clearButtonLabelCoordinates.add(
                            AreaButton(
                                startIndex,
                                true
                            )
                        )
                    }

                    drawAreaLabel(isClearAreaButton, index, canvas, rectanglePointList)
                } else {
                    val newLabelPoint = moveRectangleButtonLabelIfPossible(
                        startIndex,
                        vertices,
                        closeButton[index],
                        index + 1,
                        isAreaClearButton = isClearAreaButton
                    )
                    drawAreaLabel(isClearAreaButton, index, canvas, newLabelPoint.rectangle)
                    if (clearButtonRectangleCoordinates.isEmpty()) {
                        clearButtonRectangleCoordinates.add(
                            Pair(
                                newLabelPoint.rectanglePointIndex,
                                newLabelPoint.drawDifference
                            )
                        )
                    } else {
                        if (index > clearButtonRectangleCoordinates.lastIndex) {
                            clearButtonRectangleCoordinates.add(
                                Pair(
                                    newLabelPoint.rectanglePointIndex,
                                    newLabelPoint.drawDifference
                                )
                            )
                        } else {
                            clearButtonRectangleCoordinates[index] =
                                Pair(
                                    newLabelPoint.rectanglePointIndex,
                                    newLabelPoint.drawDifference
                                )
                        }
                    }

                    if (clearButtonLabelCoordinates.isEmpty()) {
                        clearButtonLabelCoordinates.add(
                            AreaButton(
                                newLabelPoint.outlinePointIndex,
                                true
                            )
                        )
                    } else {
                        if (index > clearButtonLabelCoordinates.lastIndex) {
                            clearButtonLabelCoordinates.add(
                                AreaButton(
                                    newLabelPoint.outlinePointIndex,
                                    true
                                )
                            )
                        } else {
                            clearButtonLabelCoordinates[index] =
                                AreaButton(
                                    newLabelPoint.outlinePointIndex,
                                    true
                                )
                        }
                    }
                }
            }
        }
        endTime = System.currentTimeMillis()
        duration = endTime - functionStartTime
        Timber.tag("drawAreaLabels func").d("duration time = $duration")
    }

    private fun drawAreaLabel(
        isClearAreaButton: Boolean,
        index: Int,
        canvas: Canvas,
        newLabelPoint: ArrayList<Point>
    ): Canvas {
        return if (isClearAreaButton) {
            closeButton[index].drawAreaClearLabelButton(
                index + 1,
                canvas,
                greenColor,
                this,
                mode,
                newLabelPoint
            )
        } else {
            closeButton[index].drawAreaLabelButton(
                index + 1,
                canvas,
                greenColor,
                this,
                newLabelPoint
            )
        }
    }

    private fun linesIntersect(a: Point, b: Point, c: Point, d: Point): Boolean {
        val aPoint = IntersectPoint.fromPoint(a)
        val bPoint = IntersectPoint.fromPoint(b)
        val cPoint = IntersectPoint.fromPoint(c)
        val dPoint = IntersectPoint.fromPoint(d)

        return doIntersect(aPoint, bPoint, cPoint, dPoint)
    }

    fun sourceToViewCoordInt(point: Point): Point {
        val pointF = sourceToViewCoord(point.x.toFloat(), point.y.toFloat())
            ?: return Point(0, 0)
        return Point(pointF.x.toInt(), pointF.y.toInt())
    }

    private fun viewToSourceCoordInt(point: Point): Point {
        val pointF = viewToSourceCoord(point.x.toFloat(), point.y.toFloat())
        return Point(pointF!!.x.toInt(), pointF.y.toInt())
    }


    fun setVisibilityVerticesIndexes(visibilityVerticesIndexes: List<Int>) {
        this.visibilityVerticesIndexes = ArrayList(visibilityVerticesIndexes)
        showOutlines = visibilityVerticesIndexes.isNotEmpty()
        invalidate()
    }

    fun setAutoDetectedVertices(vertices: List<List<Vertices>>) {
        this.verticesList = validateAutoDetectionVertices(vertices)
        lengthWidthLabelCoordinates.clear()
        clearButtonLabelCoordinates.clear()
        clearButtonRectangleCoordinates.clear()
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
        lengthWidthLabelCoordinates.clear()
        clearButtonLabelCoordinates.clear()
        clearButtonRectangleCoordinates.clear()
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

        val verticesListToViewCoordinates = ArrayList(verticesList.map {
            ArrayList(it.map { vertix ->
                PointF(
                    (vertix.point.x.toFloat()),
                    (vertix.point.y.toFloat())
                )
            })
        })

        val removeVerticesList = ArrayList<Pair<Int, ArrayList<Int>>>()
        verticesListToViewCoordinates.forEachIndexed { verticesIndex, vertices ->
            val removePointIndexes = ArrayList<Int>()
            if (vertices.isNotEmpty()) {
                vertices.forEachIndexed { index, vertix ->
                    if (!removePointIndexes.contains(index)) {
                        var isClose = true
                        var i = if (index + 1 > vertices.lastIndex) {
                            0
                        } else {
                            index + 1
                        }

                        while (isClose) {
                            val distance = PolygonGeometry.calculateDistance(
                                vertix,
                                vertices[i]
                            )
                            if (distance.toInt() + 5 < POINTS_RANGE_PX) {
                                removePointIndexes.add(i)
                                if (removePointIndexes.size >= vertices.size) {
                                    isClose = false
                                }
                            } else {
                                isClose = false
                            }
                            if (i + 1 > vertices.lastIndex) {
                                i = 0
                            } else {
                                i += 1
                            }
                        }
                    }
                }

                val distance = PolygonGeometry.calculateDistance(
                    vertices.first(),
                    vertices.last()
                )
                if (distance.toInt() + 5 < POINTS_RANGE_PX) {
                    removePointIndexes.add(vertices.lastIndex)
                }
                if (removePointIndexes.isNotEmpty()) {
                    if (!removeVerticesList.contains(Pair(verticesIndex, removePointIndexes))) {
                        removeVerticesList.add(Pair(verticesIndex, removePointIndexes))
                    }
                }
            }
        }

        removeVerticesList.forEach { (verticesIndex, removeIndexes) ->
            if (verticesListToViewCoordinates[verticesIndex].size > 3) {
                removeIndexes.distinct().sortedByDescending { it }.forEach {
                    verticesListToViewCoordinates[verticesIndex].removeAt(it)
                }
            }
        }

        var extendedVerticesListWithPoints =
            ArrayList<ArrayList<Vertices>>()
        extendedVerticesListWithPoints = ArrayList(verticesListToViewCoordinates.map {
            ArrayList(it.map {
                Vertices(it)
            })
        })


        var adjustCycles = 0
        extendedVerticesListWithPoints.forEach { vertices ->
            if (vertices.isNotEmpty()) {
                var compareVertices = vertices[0]
                vertices.forEach { vertix ->
                    if (compareVertices != vertix) {
                        val distance =
                            PolygonGeometry.calculateDistance(compareVertices.point, vertix.point)
                        val cycles = ((distance / POINTS_RANGE_PX) - 1).toInt()
                        if (cycles >= 0) {
                            adjustCycles += cycles
                        }
                        compareVertices = vertix
                    }
                }
            }
        }

        val adjustedVerticesList = if (adjustCycles > 0) {
            var newAdjustedVerticesList =
                ArrayList<ArrayList<Vertices>>()
            for (i in 1..adjustCycles step 1) {
                val verticesList = if (i == 1) {
                    extendedVerticesListWithPoints
                } else {
                    newAdjustedVerticesList
                }
                val newVerticesList =
                    ArrayList<ArrayList<Vertices>>()
                verticesList.forEach { vertices ->
                    if (vertices.isNotEmpty()) {
                        val pointList =
                            ArrayList<Vertices>()

                        var compareVertices = vertices[0]
                        vertices.forEach { vertix ->
                            if (compareVertices == vertix) {
                                pointList.add(
                                    Vertices(
                                        vertix.point
                                    )
                                )
                            } else {
                                val distance = PolygonGeometry.calculateDistance(
                                    compareVertices.point,
                                    vertix.point
                                )
                                compareVertices = if (distance.toInt() >= POINTS_RANGE_PX * 2) {
                                    val result = addAdditionalDotsBetweenAdjustedPoints(
                                        compareVertices.point,
                                        vertix.point
                                    )
                                    result?.let {
                                        if (!pointList.contains(
                                                Vertices(
                                                    it
                                                )
                                            )
                                        ) {
                                            pointList.add(
                                                Vertices(
                                                    it
                                                )
                                            )
                                        }
                                    }
                                    pointList.add(
                                        Vertices(
                                            vertix.point
                                        )
                                    )
                                    vertix
                                } else {
                                    pointList.add(
                                        Vertices(
                                            vertix.point
                                        )
                                    )
                                    vertix
                                }
                            }
                        }

                        val newFirstPointOfList = pointList.first().point
                        val newLastPointOfList = pointList.last().point

                        val distance = PolygonGeometry.calculateDistance(
                            newLastPointOfList,
                            newFirstPointOfList
                        )
                        if (distance.toInt() >= POINTS_RANGE_PX * 2) {
                            val result = addAdditionalDotsBetweenAdjustedPoints(
                                newLastPointOfList,
                                newFirstPointOfList
                            )
                            result?.let {
                                if (!pointList.contains(
                                        Vertices(
                                            it
                                        )
                                    )
                                ) {
                                    pointList.add(
                                        Vertices(
                                            it
                                        )
                                    )
                                }
                            }
                        }

                        newVerticesList.add(pointList)
                    }
                }
                newAdjustedVerticesList = newVerticesList
            }
            newAdjustedVerticesList
        } else {
            extendedVerticesListWithPoints
        }.filter { it.size >= 3 }

        return ArrayList(adjustedVerticesList.map { ArrayList(it) })
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
        fun onVertexListChanged(
            vertices: ArrayList<ArrayList<Vertices>>?,
            closed: Boolean
        )

        fun onZoomChanged(zoom: Float)
    }

    enum class Mode {
        Draw, DrawSingle, View, ViewMeasurement, ViewStoma
    }

    companion object {
        var RADIUS = 10f
        var DISABLED_RADIUS = 8f
        const val POINTS_RANGE_PX = 15
        private const val FIRST_TWO_POINTS_MULTIPLIER = 0.5
        private const val SECOND_TWO_POINTS_MULTIPLIER = 0.2
    }

}