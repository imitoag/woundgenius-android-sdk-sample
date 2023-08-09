package com.example.samplewoundsdk.utils.data

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


object ChartRangeCalculator {

//    fun calculateRange(
//        referenceDate: Date = Date(),
//        rangeType: ChartRangeType
//    ): DateManager.DateRange {
//        val endOfDay = DateManager().atEndOfDay(referenceDate)
//        var startDate = Date()
//
//        val calendar = Calendar.getInstance()
//        calendar.time = endOfDay
//        when (rangeType) {
//            ChartRangeType.day -> {
//                calendar.add(Calendar.HOUR, -24)
//                startDate = calendar.time
//            }
//            ChartRangeType.week -> {
//                calendar.add(Calendar.DATE, -7)
//                startDate = calendar.time
//            }
//            ChartRangeType.month -> {
//                calendar.add(Calendar.MONTH, -1)
//                startDate = calendar.time
//            }
//            ChartRangeType.year -> {
//                calendar.add(Calendar.YEAR, -1)
//                startDate = calendar.time
//            }
//        }
//
//        return DateManager.DateRange(startDate, endOfDay)
//    }

    fun timeStep(rangeType: ChartRangeType): Long {
        return when (rangeType) {
            ChartRangeType.day -> 3600
            ChartRangeType.week, ChartRangeType.month -> 24 * 3600
            ChartRangeType.year -> 30 * 24 * 3600    //30 days hard-coded 3600 for now
        }
    }

    fun granularity(rangeType: ChartRangeType): Float {
        return when (rangeType) {
            ChartRangeType.day -> 3600 * 8
            ChartRangeType.week -> 3600 * 24
            ChartRangeType.month -> 3600 * 24
            ChartRangeType.year -> 3600 * 24 * 30
        }.toFloat()
    }

    fun minMaxXRange(
        firstValue: Float,
        lastValue: Float,
        rangeType: ChartRangeType
    ): Pair<Long, Long> {
        val firstTimestamp = firstValue.toLong()// * 1000L
        val lastTimestamp = lastValue.toLong()// * 1000L
        return when (rangeType) {
            ChartRangeType.day -> {
                val start = Calendar.getInstance()
                start.timeInMillis = firstTimestamp
                start.add(Calendar.HOUR, -26)
                val end = Calendar.getInstance()
                end.timeInMillis = lastTimestamp
                end.add(Calendar.HOUR, +4)

                Pair(start.timeInMillis / 1000, end.timeInMillis / 1000)
            }
            ChartRangeType.week -> {
                val start = Calendar.getInstance()
                start.timeInMillis = firstTimestamp
                start.add(Calendar.DAY_OF_YEAR, -2)
                val end = Calendar.getInstance()
                end.timeInMillis = lastTimestamp
                end.add(Calendar.DAY_OF_YEAR, +2)

                Pair(start.timeInMillis / 1000, end.timeInMillis / 1000)
            }
            ChartRangeType.month -> {
                val start = Calendar.getInstance()
                start.timeInMillis = firstTimestamp
//                start.add(Calendar.MONTH, -1)
                start.add(Calendar.DAY_OF_YEAR, -3)
                val end = Calendar.getInstance()
                end.timeInMillis = lastTimestamp
                end.add(Calendar.DAY_OF_YEAR, +3)

                Pair(start.timeInMillis / 1000, end.timeInMillis / 1000)
            }
            ChartRangeType.year -> {
                val start = Calendar.getInstance()
                start.timeInMillis = firstTimestamp
//                start.add(Calendar.YEAR, -1)
                start.add(Calendar.DAY_OF_YEAR, -1)
                val end = Calendar.getInstance()
                end.timeInMillis = lastTimestamp
                end.add(Calendar.DAY_OF_YEAR, +1)

//                Pair(start.timeInMillis / 1000, end.timeInMillis / 1000)
                Pair(start.timeInMillis, end.timeInMillis)
            }
        }
    }

    fun zoomFactor(rangeType: ChartRangeType): Float {
        return when (rangeType) {
            ChartRangeType.day -> {
                //split by hour
                12f
            }
            ChartRangeType.week -> {
                //split by day
                9f
            }
            ChartRangeType.month -> {
                //split by week
                3f
            }
            ChartRangeType.year -> {
                //split by month
                2f
            }
        }
    }

    fun getGraphAxisFormatter(range: ChartRangeType): ValueFormatter {
        return when (range) {
            ChartRangeType.day -> ChartHourValueFormatter()
            ChartRangeType.week -> ChartDateValueFormatter()
            ChartRangeType.month -> ChartDateValueFormatter()
            ChartRangeType.year -> ChartDateValueFormatter()
        }
    }

//    fun timestampToString(taskResult: TaskResult, range: ChartRangeType): String {
//        return when (range) {
//            ChartRangeType.day -> ChartDateValueFormatter().makePretty(taskResult.getDateInMillis())
//            ChartRangeType.week -> ChartDateValueFormatter().makePretty(taskResult.getDateInMillis())
//            ChartRangeType.month -> ChartDateValueFormatter().makePretty(taskResult.getDateInMillis())
//            ChartRangeType.year -> ChartMonthValueFormatter().makePretty(taskResult.getDateInMillis())
//        }
//    }

    class ChartMonthValueFormatter : ValueFormatter() {

        private val serverDateFormatter by lazy {
            SimpleDateFormat(YEAR_MONTH_PATTERN, Locale.getDefault())
        }
        private val YEAR_MONTH_PATTERN = "YYYY MMM"


        override fun getFormattedValue(value: Float): String {
            return makePretty(value.toLong() * 1000L)
        }

        fun makePretty(timeStamp: Long): String {
            val date = Date(timeStamp)
            return serverDateFormatter.format(date)
        }
    }

    class ChartDateValueFormatter : ValueFormatter() {

        private val serverDateFormatter by lazy {
            SimpleDateFormat(MONTH_PATTERN, Locale.getDefault())
        }
        private val MONTH_PATTERN = "dd MMM"

        override fun getFormattedValue(value: Float): String {
//            return makePretty(value.toLong() * 1000L)
            return makePretty(value.toLong())
        }

        fun makePretty(timeStamp: Long): String {
            val date = Date(timeStamp)
            return serverDateFormatter.format(date)
        }
    }

    class ChartHourValueFormatter : ValueFormatter() {

        private val serverDateFormatter by lazy {
            SimpleDateFormat(HOUR_PATTERN, Locale.getDefault())
        }
        private val HOUR_PATTERN = "HH"


        override fun getFormattedValue(value: Float): String {
            return makePretty(value.toLong() * 1000L)
        }

        private fun makePretty(timeStamp: Long): String {
            val date = Date(timeStamp)
            return serverDateFormatter.format(date)
        }
    }


}