package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.CalendarDatabase
import com.example.myapplication.data.CalendarEventRepository

class CalendarApplication : Application() {
    val database: CalendarDatabase by lazy {
        CalendarDatabase.getInstance(this)
    }

    val repository: CalendarEventRepository by lazy {
        CalendarEventRepository(database.calendarEventDao())
    }
}


