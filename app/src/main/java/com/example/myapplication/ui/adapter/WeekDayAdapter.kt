package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemWeekDayBinding
import com.example.myapplication.databinding.ItemWeekEventBinding
import com.example.myapplication.ui.viewmodel.CalendarViewModel
import com.example.myapplication.util.TimeUtils
import java.time.format.TextStyle
import java.util.Locale

class WeekDayAdapter(
    private val onEventClick: (Long) -> Unit
) : RecyclerView.Adapter<WeekDayAdapter.WeekDayViewHolder>() {

    private val items = mutableListOf<CalendarViewModel.DayWithEvents>()

    fun submitList(newItems: List<CalendarViewModel.DayWithEvents>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekDayViewHolder {
        val binding = ItemWeekDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeekDayViewHolder(binding, onEventClick)
    }

    override fun onBindViewHolder(holder: WeekDayViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class WeekDayViewHolder(
        private val binding: ItemWeekDayBinding,
        private val onEventClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarViewModel.DayWithEvents) {
            binding.weekDayTitle.text = "${item.date.monthValue}/${item.date.dayOfMonth}"
            binding.weekDaySubtitle.text = item.date.dayOfWeek.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )

            binding.weekDayEvents.removeAllViews()
            binding.weekDayEmpty.isVisible = item.events.isEmpty()

            item.events.forEach { event ->
                val eventBinding = ItemWeekEventBinding.inflate(
                    LayoutInflater.from(binding.root.context),
                    binding.weekDayEvents,
                    false
                )
                eventBinding.weekEventTitle.text = event.title
                eventBinding.weekEventTime.text = TimeUtils.timeRangeLabel(
                    event.startEpochMillis,
                    event.endEpochMillis,
                    event.allDay
                )
                eventBinding.weekEventLocation.text = event.location.orEmpty()
                eventBinding.weekEventLocation.isVisible = !event.location.isNullOrBlank()
                eventBinding.root.setOnClickListener { onEventClick(event.id) }
                binding.weekDayEvents.addView(eventBinding.root)
            }
        }
    }
}

