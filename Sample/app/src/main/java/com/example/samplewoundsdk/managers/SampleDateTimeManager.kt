package com.example.samplewoundsdk.managers

interface SampleDateTimeManager {

    fun convertTimestampToTime(timestamp: Long): String?

    fun convertTimestampToServerDateTime(timestamp: Long): String?

    fun convertTimestampToServerDate(timeStamp: Long): String?

    fun convertTimestampToDayMonthYearDate(timeStamp: Long): String?

    fun convertServerDateToDayMonthYearDate(dateTime: String): String?

    fun convertServerDateToTimestamp(dateTime: String): Long?

    fun convertServerDateTimeToDayMonthYearDate(dateTime: String): String?

    fun convertServerDateTimeToTime(dateTime: String): String?

    fun convertServerDateTimeToChangeableDate(dateTime: String): String?

    fun convertServerDateTimeToTimestamp(dateTime: String): Long?

    fun convertDayMonthYearDateToTimestamp(date: String): Long?

}
