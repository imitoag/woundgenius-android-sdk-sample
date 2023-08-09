package com.example.samplewoundsdk.managers.impl

import android.content.res.Resources
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.managers.SampleDateTimeManager
import java.text.SimpleDateFormat
import java.util.*

class SampleDateTimeManagerImpl(
    private val resources: Resources
) : SampleDateTimeManager {

    private val serverDateFormatter
        get() = SimpleDateFormat(
            SERVER_DATE_PATTERN,
            Locale.getDefault()
        )
    private val serverDateTimeFormatter
        get() = SimpleDateFormat(SERVER_DATE_TIME_PATTERN, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    private val dayMonthYearFormatter
        get() = SimpleDateFormat(
            resources.getString(R.string.day_month_year_date_pattern),
            Locale.getDefault()
        )
    private val dayMonthFormatter
        get() = SimpleDateFormat(
            resources.getString(R.string.day_month_date_pattern),
            Locale.getDefault()
        )
    private val yearFormatter
        get() = SimpleDateFormat(
            resources.getString(R.string.year_pattern),
            Locale.getDefault()
        )
    private val timeFormatter
        get() = SimpleDateFormat(
            resources.getString(R.string.time_pattern),
            Locale.getDefault()
        )

    override fun convertTimestampToTime(timestamp: Long): String? {
        val date = Date(timestamp)
        return timeFormatter.format(date)
    }

    override fun convertTimestampToServerDateTime(timestamp: Long): String? {
        val date = Date(timestamp)
        return serverDateTimeFormatter.format(date)
    }

    override fun convertTimestampToServerDate(timeStamp: Long): String? {
        val date = Date(timeStamp)
        return serverDateFormatter.format(date)
    }

    override fun convertTimestampToDayMonthYearDate(timeStamp: Long): String? {
        val date = Date(timeStamp)
        return dayMonthYearFormatter.format(date)
    }


    override fun convertServerDateToDayMonthYearDate(dateTime: String): String? {
        val date = serverDateFormatter.parse(dateTime)
        return date?.let { dayMonthYearFormatter.format(it) }
    }

    override fun convertServerDateToTimestamp(dateTime: String): Long? {
        return serverDateFormatter.parse(dateTime)?.time
    }


    override fun convertServerDateTimeToDayMonthYearDate(dateTime: String): String? {
        val date = serverDateTimeFormatter.parse(dateTime)
        return date?.let { dayMonthYearFormatter.format(it) }
    }

    override fun convertServerDateTimeToTime(dateTime: String): String? {
        val date = serverDateTimeFormatter.parse(dateTime)
        return date?.let { timeFormatter.format(date) }
    }

    override fun convertServerDateTimeToChangeableDate(dateTime: String): String? {
        val serverDate = serverDateTimeFormatter.parse(dateTime)
        return serverDate?.let { date ->
            val dayMonthDayDate = dayMonthYearFormatter.format(date)
            when {
                dayMonthDayDate == dayMonthYearFormatter.format(Date()) -> {
                    timeFormatter.format(date)
                }
                yearFormatter.format(date) == yearFormatter.format(Date()) -> {
                    dayMonthFormatter.format(date)
                }
                else -> {
                    dayMonthDayDate
                }
            }
        }
    }

    override fun convertServerDateTimeToTimestamp(dateTime: String): Long? {
        return serverDateTimeFormatter.parse(dateTime)?.time
    }


    override fun convertDayMonthYearDateToTimestamp(date: String): Long? {
        return dayMonthYearFormatter.parse(date)?.time
    }


    companion object {
        private const val SERVER_DATE_PATTERN = "yyyy-MM-dd"
        private const val SERVER_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
    }

}
