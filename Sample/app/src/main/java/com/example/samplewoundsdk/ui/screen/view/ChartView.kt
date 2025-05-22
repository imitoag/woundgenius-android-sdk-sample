//package com.example.samplewoundsdk.ui.screen.view
//
//import android.content.Context
//import android.graphics.Color
//import android.view.View
//import com.example.woundsdk.R
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class ChartView(context: Context) : LineChart(context) {
//
//    private val SERVER_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
//
//    init {
//        setupChart()
//    }
//
//    private fun setupChart() {
//        description.isEnabled = false
//        setScaleEnabled(false)
////        isPinchZoomEnabled = false
//        isHighlightPerDragEnabled = false
//        isDoubleTapToZoomEnabled = false
////        isUserInteractionEnabled = false
//
//        axisRight.isEnabled = false
//        legend.isEnabled = false
//
//        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.setDrawGridLines(false)
//        xAxis.valueFormatter = DateValueFormatter()
//        xAxis.granularity = 86400f // 60 * 60 * 24 (один день)
//    }
//
//    fun updateChartData(assessmentList: List<SampleAssessmentEntity>) {
//        // Отбираем только те series, у которых ровно одно измерение
//        val filtered = assessmentList.filter { assessment ->
//            assessment.media?.filter {
//                it.metadata?.measurementData != null
//            }?.size == 1
//        }
//
//        if (filtered.size < 2) {
//            visibility = View.GONE
//            return
//        }
//
//        val entries = filtered.map { assessment ->
//            val measurement = assessment.media?.first {
//                it.metadata?.measurementData != null
//            }
//
//            val totalArea =
//                measurement?.metadata?.measurementData?.annotationList?.mapNotNull { it?.area }
//                    ?.sum()
//
//            val format = SimpleDateFormat(SERVER_DATE_TIME_PATTERN, Locale.ENGLISH)
//            val date = format.parse(assessment.datetime?:"")
//            val timestamp = date?.time
//            Entry(timestamp?.toFloat() ?: 0.0f  , totalArea?.toFloat() ?: 0.0f)
//        }
//
//        val dataSet =
//            LineDataSet(entries, context.getString(R.string.WOUND_GENIUS_SDK_AREA)).apply {
//                color = Color.RED
//                setCircleColor(Color.RED)
//                lineWidth = 1f
//                circleRadius = 2f
//                setDrawCircleHole(false)
//                valueTextSize = 8f
//                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
//                highLightColor = Color.RED
//            }
//
//        if (filtered.size >= 2) {
//            val format = SimpleDateFormat(SERVER_DATE_TIME_PATTERN, Locale.ENGLISH)
//            val firstDate = format.parse( filtered.first().datetime?:"")
//            val lastDate = format.parse( filtered.last().datetime?:"")
//            val first = firstDate?.time ?: 0L
//            val last = lastDate?.time ?: 0L
//
//            xAxis.spaceMin = ((last - first) * 0.03).toFloat()
//            xAxis.spaceMax = ((last - first) * 0.04).toFloat()
//        }
//
//        data = LineData(dataSet)
//        highlightValue(entries.last().x, entries.last().y, 0)
//        visibility = View.VISIBLE
//    }
//
//    class DateValueFormatter : ValueFormatter() {
//        private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
//        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
//            return dateFormat.format(Date(value.toLong() * 1000)) // переводим в миллисекунды
//        }
//    }
//}
