package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Query(
        """
            SELECT * FROM calendar_events
            WHERE startEpochMillis < :endMillis AND endEpochMillis > :startMillis
            ORDER BY startEpochMillis ASC
        """
    )
    fun observeEventsBetween(startMillis: Long, endMillis: Long): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events WHERE id = :eventId LIMIT 1")
    suspend fun getEvent(eventId: Long): CalendarEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: CalendarEvent): Long

    @Delete
    suspend fun delete(event: CalendarEvent)
}


