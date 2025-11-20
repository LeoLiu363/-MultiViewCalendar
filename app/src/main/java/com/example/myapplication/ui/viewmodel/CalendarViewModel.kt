package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CalendarEvent
import com.example.myapplication.data.CalendarEventRepository
import com.example.myapplication.util.TimeUtils
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val repository: CalendarEventRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _weekAnchorDate = MutableStateFlow(LocalDate.now())
    val weekAnchorDate: StateFlow<LocalDate> = _weekAnchorDate.asStateFlow()

    val dayEvents: StateFlow<List<CalendarEvent>> =
        selectedDate.flatMapLatest { repository.observeEventsForDay(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weekDaysWithEvents: StateFlow<List<DayWithEvents>> =
        weekAnchorDate.flatMapLatest { anchor ->
            repository.observeEventsForWeek(anchor).map { events ->
                val startOfWeek = anchor.minusDays(((anchor.dayOfWeek.value + 6) % 7).toLong())
                (0 until 7).map { offset ->
                    val date = startOfWeek.plusDays(offset.toLong())
                    val dayBounds = TimeUtils.dayBounds(date)
                    val dayEvents = events.filter { event ->
                        event.startEpochMillis < dayBounds.second && event.endEpochMillis > dayBounds.first
                    }
                    DayWithEvents(date, dayEvents)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _weekAnchorDate.value = date
    }

    fun moveDay(deltaDays: Long) {
        _selectedDate.value = _selectedDate.value.plusDays(deltaDays)
    }

    fun moveWeek(deltaWeeks: Long) {
        _weekAnchorDate.value = _weekAnchorDate.value.plusWeeks(deltaWeeks)
    }

    data class DayWithEvents(
        val date: LocalDate,
        val events: List<CalendarEvent>
    )

    class Factory(
        private val repository: CalendarEventRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                return CalendarViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

