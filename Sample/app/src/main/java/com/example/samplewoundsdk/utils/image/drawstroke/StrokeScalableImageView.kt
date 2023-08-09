package com.example.samplewoundsdk.utils.image.drawstroke

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.samplewoundsdk.R

class StrokeScalableImageView : SubsamplingScaleImageView {

    var circleRadius = 0f
    var textSize = 0f
    var lineWidth = 0f
    var minDistance = 0f
    var greenColor = 0
    var transparentGreenColor = 0
    private val polygonPath = Path()
    private val fingerLinePath = Path()
    var vertices = ArrayList<ArrayList<Point>>().apply {
        add(ArrayList())
    }
    var visibilityVerticesIndexes = ArrayList(vertices.mapIndexed { index, _ -> index })
    private var vertexPaint: Paint? = null
    private var vertexStrokePaint: Paint? = null
    private var pathPaint: Paint? = null
    private var linesPaint: Paint? = null
    private var textPaint: Paint? = null
    private var linesStrokePaint: Paint? = null
    private val currentPosition = Point()
    private var firstPoint = Point()
    var isPathClosed = ArrayList(vertices.map { false })
    private var currentDraggedPoint: Point? = null
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
        circleRadius = context.resources.getDimension(R.dimen.stroke_circle_radius)
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
        vertexStrokePaint = initVertexStrokePaint()
        pathPaint = initPathPaint()
        fillPathPaint = initFillPathPaint()
        clearPaint = initClearPaint()
        closeButton.add(CloseButton(circleRadius.toInt(), context))
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

    fun setTouchListener(touchListener: ViewTouchListener?) {
        this.touchListener = touchListener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1 || (mode != Mode.Draw && mode != Mode.DrawSingle)) {
            if (vertices.lastOrNull()?.size == 1) {
                vertices.lastOrNull()?.clear()
            }
            return super.onTouchEvent(event)
        }
        if (System.currentTimeMillis() - event.downTime < 150) {
            return true
        }
        if (mode != Mode.Draw && mode != Mode.DrawSingle) return true
        var x = event.x
        var y = event.y
        val pointF = viewToSourceCoord(x, y)
        if (pointF != null) {
            x = pointF.x
            y = pointF.y
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y, event.x, event.y)
                if (touchListener != null && pointF != null) touchListener!!.onDown(pointF)
                if (touchListener != null) touchListener!!.onVertexListChanged(
                    vertices,
                    isAllPathClosed()
                )
                postInvalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                if (touchListener != null && pointF != null) touchListener!!.onMove(pointF)
                if (touchListener != null) touchListener!!.onVertexListChanged(
                    vertices,
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
        return true
    }

    private fun isAllPathClosed(): Boolean {
        var isAllPathClosed = true
        isPathClosed.forEachIndexed { index, b ->
            if (!(index == isPathClosed.lastIndex && vertices.lastOrNull().isNullOrEmpty())) {
                if (!b) {
                    isAllPathClosed = false
                }
            }
        }
        return if (vertices.size == 1) {
            isPathClosed.last()
        } else {
            isAllPathClosed
        }
    }

    private fun isPointCanBeMoved() =
        ((isPathClosed.size >= 2 && isPathClosed[isPathClosed.lastIndex - 1])) && vertices.lastOrNull()
            .isNullOrEmpty()

    private fun touchStart(x: Float, y: Float, originalX: Float, originalY: Float) {
        isTouchUP = false
        if (isPointCanBeMoved()) {
            currentDraggedPoint = findNearestPoint(x, y)
        }
        closeButton.forEachIndexed { index, closeButton ->
            if (closeButton.center != null) {
                if (processClearClick(originalX, originalY, closeButton, index)) {
                    return
                }
            }
        }
        if (findNearestPoint(x, y) == null && !isClear) {
            if (vertices.size > 1 && mode == Mode.DrawSingle) {
                return
            }
            vertices.lastOrNull()?.add(Point(x.toInt(), y.toInt()))
        }
    }

    private fun touchUp(x: Float, y: Float) {
        isTouchUP = true
        if (!isPathClosed.last() && currentDraggedPoint == null && !isClear && (vertices.lastOrNull()?.size
                ?: 0) > 0
        ) {
            if (!(vertices.size > 1 && mode == Mode.DrawSingle)) {
                vertices.lastOrNull()?.add(Point(x.toInt(), y.toInt()))
            }
        }

        if (isPointCanBeMoved() && currentDraggedPoint != null) {
            currentDraggedPoint!!.x = x.toInt()
            currentDraggedPoint!!.y = y.toInt()
        } else if (currentDraggedPoint != null) drawPath(x, y)
    }

    private fun touchMove(x: Float, y: Float) {
        if (isPointCanBeMoved() && currentDraggedPoint != null) {
            currentDraggedPoint!!.x = x.toInt()
            currentDraggedPoint!!.y = y.toInt()
        } else {
            drawPath(x, y)
        }
    }

    private fun findNearestPoint(x: Float, y: Float): Point? {
        val distances = SparseArray<Point?>()
        val position = Point(x.toInt(), y.toInt())
        var nearestPoint: Point? = null
        vertices.forEach {
            it.forEach { point ->
                val calculateDistance =
                    PolygonGeometry.calculateDistance(
                        point,
                        position
                    ).toInt()
                if (calculateDistance < TOUCH_SENSETIVE / scale) {
                    distances.put(calculateDistance, point)
                }
            }
        }
        if (distances.size() == 0) return null
        val keyAt = distances.keyAt(0)
        nearestPoint = distances[keyAt, null]
        return nearestPoint
    }

    private fun drawPath(x: Float, y: Float) {
        try {
            var x = x
            var y = y
            var p: PointF? = PointF(x, y)
            p = sourceToViewCoord(p)
            if (p != null) {
                x = p.x
                y = p.y
                currentPosition.x = p.x.toInt()
                currentPosition.y = p.y.toInt()
                if (vertices.lastOrNull()?.size ?: 0 > 0) {
                    var lastPoint = vertices.lastOrNull()?.lastIndex?.let {
                        vertices.lastOrNull()?.get(
                            it
                        )
                    }
                    lastPoint = lastPoint?.let { sourceToViewCoordInt(it) }
                    val distance = PolygonGeometry.calculateDistance(lastPoint, currentPosition)
                    if (distance >= minDistance) {
                        val pointF = viewToSourceCoord(x, y)
                        vertices.lastOrNull()?.add(Point(pointF!!.x.toInt(), pointF.y.toInt()))
                    }
                    fingerLinePath.reset()
                    fingerLinePath.moveTo(
                        lastPoint?.x?.toFloat() ?: 0f,
                        lastPoint?.y?.toFloat() ?: 0f
                    ) //!!!
                    fingerLinePath.lineTo(x, y)
                    firstPoint = vertices.lastOrNull()?.get(0) ?: Point(0, 0)
                    val delta = (TOUCH_SENSETIVE / scale).toInt()
                    p = viewToSourceCoord(p)
                    x = p!!.x
                    y = p.y
                    val isXaround = x < firstPoint.x + delta && x > firstPoint.x - delta
                    val isYaround = y < firstPoint.y + delta && y > firstPoint.y - delta
                    if (isXaround && isYaround && vertices.lastOrNull()?.size ?: 0 > 2) {
                        fingerLinePath.reset()
                        isPathClosed[isPathClosed.lastIndex] = true
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
        vertices.removeAt(index)
        isPathClosed.removeAt(index)
        closeButton.removeAt(index)
        isClear = true
        invalidate()
    }

    private fun doOnDrawWork(canvas: Canvas, isTimelineFragment: Boolean) {
        if (!isClear) {
            vertices.forEachIndexed { index, vertices ->
                if (index != this.vertices.lastIndex && this.vertices.lastIndex != -1) {
                    drawWidthAndLength(
                        canvas,
                        vertices,
                        widthIndexes?.get(index),
                        lengthIndexes?.get(index)
                    )
                    drawPolygon(canvas, vertices, isTimelineFragment) //green dots
                    closePath(
                        canvas,
                        vertices,
                        isPathClosed[index],
                        isTimelineFragment
                    ) //connects first and last points of boundary
                    canvas.drawPath(polygonPath, pathPaint!!) //green lines between dots
                    if (isNeedWhiteStroke) { //white borders of green dots
                        drawVertexStrokes(canvas, vertices, isTimelineFragment)
                    }
                    drawClearSymbol(canvas, closeButton[index], index + 1, vertices)
                    polygonPath.reset()
                }
            }
            drawPolygon(canvas)
            closePath(
                canvas,
                vertices.lastOrNull() ?: ArrayList(),
                isPathClosed.last(),
                isTimelineFragment
            )
            canvas.drawPath(polygonPath, pathPaint!!)
            if (isNeedWhiteStroke) drawVertexStrokes(
                canvas,
                vertices.lastOrNull() ?: ArrayList(),
                isTimelineFragment
            )
            drawLineByFinger(canvas, vertices.lastOrNull() ?: ArrayList(), isTimelineFragment)
            if (isTouchUP && !polygonPath.isEmpty && isPathClosed.last()) {
                drawClearSymbol(
                    canvas,
                    closeButton.last(),
                    vertices.lastIndex + 1,
                    vertices.lastOrNull() ?: ArrayList()
                )
                isTouchUP = false
                isPathClosed.add(false)
                closeButton.add(CloseButton(circleRadius.toInt(), context))
                vertices.add(ArrayList())
                visibilityVerticesIndexes.add(vertices.lastIndex)
            }

            polygonPath.reset()
        } else {
            polygonPath.reset()
            fingerLinePath.reset()
            isClear = false
            touchListener!!.onVertexListChanged(vertices, isAllPathClosed())
            invalidate()
        }
    }

    private fun doOnRepresent(canvas: Canvas, isTimelineFragment: Boolean) {
        this.vertices.forEachIndexed { index, vertices ->
            if (vertices.isNotEmpty() && visibilityVerticesIndexes.contains(index)) {
                val widthIndex = widthIndexes?.get(index)
                val lengthIndex = lengthIndexes?.get(index)
                drawWidthAndLength(canvas, vertices, widthIndex, lengthIndex)
                drawPolygon(canvas, vertices, isTimelineFragment) //green dots
                closePath(
                    canvas,
                    vertices,
                    isPathClosed[index],
                    isTimelineFragment
                ) //connects first and last points of boundary
                canvas.drawPath(polygonPath, pathPaint!!) //green lines between dots
                if (isNeedWhiteStroke) {
                    drawVertexStrokes(canvas, vertices, isTimelineFragment)
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
                } catch (e: Exception) {

                }
                polygonPath.reset()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, 0f, 0f, clearPaint!!)
        when (mode) {
            Mode.Draw -> {
                doOnDrawWork(canvas, false)
            }
            Mode.DrawSingle -> {
                doOnDrawWork(canvas, false)
            }
            Mode.ViewMeasurement -> {
                doOnRepresent(canvas, true)
            }
            Mode.ViewStoma -> {
                doOnRepresent(canvas, true)
            }
            else -> {}
        }
        if (zoom != scale) {
            zoom = scale
            closeButton.forEach {
                it.setscale(zoom)
            }
            if (touchListener != null) touchListener!!.onZoomChanged(zoom)
        }
    }

    private fun drawPolygon(canvas: Canvas) {
        val size = vertices.lastOrNull()?.size
        if (size != null && size > 0) {
            for (i in 0 until size) {
                val vertex = vertices.lastOrNull()?.get(i)
                vertex?.let {
                    val point = sourceToViewCoordInt(it)
                    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), RADIUS, vertexPaint!!)
                    if (i > 0) {
                        polygonPath.lineTo(point.x.toFloat(), point.y.toFloat())
                    } else {
                        polygonPath.moveTo(point.x.toFloat(), point.y.toFloat())
                    }
                    if (isNeedWhiteStroke) {
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

    private fun drawPolygon(
        canvas: Canvas,
        vertices: ArrayList<Point>,
        isTimelineFragment: Boolean
    ) {
        val size = vertices.size
        if (size > 0) {
            for (i in 0 until size) {
                val vertex = vertices[i]
                vertex.let {
                    val point = sourceToViewCoordInt(it)
                    if (!isTimelineFragment) {
                        canvas.drawCircle(
                            point.x.toFloat(),
                            point.y.toFloat(),
                            RADIUS,
                            vertexPaint!!
                        )
                    }
                    if (i > 0) {
                        polygonPath.lineTo(point.x.toFloat(), point.y.toFloat())
                    } else {
                        polygonPath.moveTo(point.x.toFloat(), point.y.toFloat())
                    }
                    if (isNeedWhiteStroke && !isTimelineFragment) {
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

    private fun drawWidthAndLength(
        canvas: Canvas,
        vertices: ArrayList<Point>,
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
        vertices: ArrayList<Point>,
        indexes: Pair<Int?, Int?>,
        letter: String,
    ) {
        try {
            val widthA = vertices[indexes.first ?: 0]
            val widthB = vertices[indexes.second ?: 0]
            widthA.let { widthA ->
                val pointA = sourceToViewCoordInt(widthA)

                widthB.let { widthB ->
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
        vertices: ArrayList<Point>,
        isPathClosed: Boolean,
        isTimelineFragment: Boolean
    ) {
        if (isPathClosed) {
            var point = vertices.lastOrNull()
            point?.let {
                val point = sourceToViewCoordInt(it)
                polygonPath.lineTo(point.x.toFloat(), point.y.toFloat())
                polygonPath.close()
                if (mode == Mode.Draw || mode == Mode.DrawSingle) {
                    canvas.drawPath(polygonPath, fillPathPaint!!)
                    if (isNeedWhiteStroke) {
                        drawVertexStrokes(canvas, vertices, isTimelineFragment)
                    }
                }
            }
        }
    }

    private fun drawLineByFinger(
        canvas: Canvas,
        vertices: ArrayList<Point>,
        isTimelineFragment: Boolean
    ) {
        if (!isPathClosed.last()) {
            canvas.drawPath(fingerLinePath, pathPaint!!)
            if (isNeedWhiteStroke) {
                drawVertexStrokes(canvas, vertices, isTimelineFragment)
            }
        }
    }

    private fun drawClearSymbol(
        canvas: Canvas,
        closeButton: CloseButton,
        number: Int,
        vertices: ArrayList<Point>
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

    @JvmName("getVertices1")
    fun getVertices(): ArrayList<ArrayList<Point>> {
        val v = ArrayList<ArrayList<Point>>()
        for (vertice in vertices) {
            v.add(vertice)
        }
        return v
    }

    fun setVisibilityVerticesIndexes(visibilityVerticesIndexes: List<Int>) {
        this.visibilityVerticesIndexes = ArrayList(visibilityVerticesIndexes)
        invalidate()
    }

    fun setVertices(vertices: List<List<Point>>) {
        this.vertices = ArrayList(vertices.map { ArrayList(it) })
        visibilityVerticesIndexes = ArrayList(this.vertices.mapIndexed { index, _ -> index })
        isPathClosed = ArrayList(this.vertices.map { it.isNotEmpty() })
        closeButton = ArrayList(this.vertices.map { CloseButton(circleRadius.toInt(), context) })
        if (mode == Mode.Draw || mode == Mode.DrawSingle) {
            isTouchUP = true
        }
        touchListener?.onVertexListChanged(this.vertices, isAllPathClosed())
        invalidate()
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

    fun getPathPaint(): Paint {
        return Paint(pathPaint)
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
        vertices: ArrayList<Point>,
        isTimelineFragment: Boolean
    ) {
        val size = vertices.size
        if (size != null && size > 0) {
            for (i in 0 until size) {
                var vertex = vertices[i]
                vertex.let { vertex ->
                    val point = sourceToViewCoordInt(vertex)
                    if (!isTimelineFragment) {
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

    interface ViewTouchListener {
        fun onDown(sourceCoords: PointF?)
        fun onMove(viewCoord: PointF?)
        fun onUp()
        fun onVertexListChanged(vertices: ArrayList<ArrayList<Point>>?, closed: Boolean)
        fun onZoomChanged(zoom: Float)
    }

    enum class Mode {
        Draw, DrawSingle, View, ViewMeasurement, ViewStoma
    }

    companion object {
        var RADIUS = 10f
        const val TOUCH_SENSETIVE = 50
    }

}