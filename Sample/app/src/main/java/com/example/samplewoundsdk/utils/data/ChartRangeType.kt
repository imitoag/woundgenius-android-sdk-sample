package com.example.samplewoundsdk.utils.data

import java.util.*

enum class ChartRangeType {
    day, week, month, year
}

fun ChartRangeType.getStartRangeTimestamp(): Long {

    val calendar = Calendar.getInstance()
    when (this) {
        ChartRangeType.day -> calendar.add(Calendar.DAY_OF_YEAR, -1)
        ChartRangeType.week -> calendar.add(Calendar.DAY_OF_YEAR, -7)
        ChartRangeType.month -> calendar.add(Calendar.MONTH, -1)
        ChartRangeType.year -> calendar.add(Calendar.YEAR, -1)
    }
    return calendar.timeInMillis
}