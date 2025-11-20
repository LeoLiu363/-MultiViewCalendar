package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val reminderMinutesBefore: Int? = null,
    val allDay: Boolean = false
)


