package com.example.myapplication.data

import com.example.myapplication.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CalendarEventRepository(
    private val dao: CalendarEventDao
) {

    fun observeEventsForDay(date: LocalDate): Flow<List<CalendarEvent>> {
        val range = TimeUtils.dayBounds(date)
        return observeEventsBetween(range.first, range.second)
    }

    fun observeEventsForWeek(anchorDate: LocalDate): Flow<List<CalendarEvent>> {
        val range = TimeUtils.weekBounds(anchorDate)
        return observeEventsBetween(range.first, range.second)
    }

    private fun observeEventsBetween(startMillis: Long, endMillis: Long): Flow<List<CalendarEvent>> {
        return dao.observeEventsBetween(startMillis, endMillis)
    }

    suspend fun getEvent(id: Long): CalendarEvent? = dao.getEvent(id)

    suspend fun upsert(event: CalendarEvent): Long = dao.upsert(event)

    suspend fun delete(event: CalendarEvent) = dao.delete(event)

    suspend fun deleteById(id: Long) {
        dao.getEvent(id)?.let { dao.delete(it) }
    }
}

