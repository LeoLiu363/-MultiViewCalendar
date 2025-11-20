package com.example.myapplication.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtils {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    fun nowMillis(): Long = LocalDateTime.now().atZone(zoneId).toInstant().toEpochMilli()

    fun dateLabel(date: LocalDate): String = dateFormatter.format(date)

    fun timeRangeLabel(startMillis: Long, endMillis: Long, allDay: Boolean): String {
        if (allDay) return "全天"
        val startTime = Instant.ofEpochMilli(startMillis).atZone(zoneId).toLocalTime()
        val endTime = Instant.ofEpochMilli(endMillis).atZone(zoneId).toLocalTime()
        return "${timeFormatter.format(startTime)} - ${timeFormatter.format(endTime)}"
    }

    fun dayBounds(date: LocalDate): Pair<Long, Long> {
        val start = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return start to end
    }

    fun weekBounds(anchorDate: LocalDate): Pair<Long, Long> {
        val startOfWeek = anchorDate.minusDays(((anchorDate.dayOfWeek.value + 6) % 7).toLong())
        val startMillis = startOfWeek.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = startOfWeek.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return startMillis to endMillis
    }

    fun dateTimeToMillis(date: LocalDate, hour: Int, minute: Int): Long {
        return LocalDateTime.of(date.year, date.month, date.dayOfMonth, hour, minute)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun millisToLocalDateTime(millis: Long): LocalDateTime =
        Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime()
}


