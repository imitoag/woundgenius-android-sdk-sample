package io.imito.woundgenius.sample.managers.impl

import android.content.res.Resources
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.managers.SampleDateTimeManager
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.SERVER_DATE_PATTERN
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.SERVER_DATE_TIME_PATTERN
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
            resources.getString(R.string.WOUND_GENIUS_SDK_day_month_year_date_pattern),
            Locale.getDefault()
        )
    private val dayMonthFormatter
        get() = SimpleDateFormat(
            resources.getString(R.string.WOUND_GENIUS_SDK_day_month_date_pattern),
            Locale.getDefault()
        )
    private val yearFormatter
        get() = SimpleDateFormat(
            resources.getString(R.string.WOUND_GENIUS_SDK_year_pattern),
            Locale.getDefault()
        )
    private val timeFormatter
        get() = SimpleDateFormat(
            resources.getString(R.string.WOUND_GENIUS_SDK_time_pattern),
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

    }

}
