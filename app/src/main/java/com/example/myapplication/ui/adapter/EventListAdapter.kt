package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.CalendarEvent
import com.example.myapplication.databinding.ItemCalendarEventBinding
import com.example.myapplication.util.TimeUtils

class EventListAdapter(
    private val onEventClick: (CalendarEvent) -> Unit
) : ListAdapter<CalendarEvent, EventListAdapter.EventViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<CalendarEvent>() {
        override fun areItemsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCalendarEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding, onEventClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val binding: ItemCalendarEventBinding,
        private val onEventClick: (CalendarEvent) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: CalendarEvent) {
            binding.eventTitle.text = event.title
            binding.eventTime.text =
                TimeUtils.timeRangeLabel(event.startEpochMillis, event.endEpochMillis, event.allDay)
            binding.eventLocation.text = event.location.orEmpty()
            binding.eventDescription.text = event.description.orEmpty()

            binding.eventLocation.isVisible = !event.location.isNullOrBlank()
            binding.eventDescription.isVisible = !event.description.isNullOrBlank()

            binding.root.setOnClickListener { onEventClick(event) }
        }
    }
}

