package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.CalendarEvent
import com.example.myapplication.databinding.ActivityEventEditorBinding
import com.example.myapplication.R
import com.example.myapplication.notifications.ReminderScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class EventEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventEditorBinding
    private val repository by lazy { (application as CalendarApplication).repository }

    private var eventId: Long? = null
    private var startMillis: Long = System.currentTimeMillis()
    private var endMillis: Long = System.currentTimeMillis() + 60 * 60 * 1000
    private var reminderMinutes: Int? = 30
    private var isAllDay: Boolean = false

    private val zoneId = ZoneId.systemDefault()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupReminderDropdown()
        setupListeners()

        eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L).takeIf { it > 0 }
        if (eventId != null) {
            binding.editorToolbar.title = getString(R.string.editor_title_edit_event)
            binding.buttonDelete.visibility = View.VISIBLE
            loadEvent(eventId!!)
        } else {
            updateDateButtons()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.editorToolbar)
        binding.editorToolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupReminderDropdown() {
        val entries = resources.getStringArray(R.array.reminder_entries)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, entries)
        binding.dropdownReminder.setAdapter(adapter)
        val values = resources.getIntArray(R.array.reminder_values)
        val defaultIndex = 2.coerceAtMost(entries.lastIndex)
        reminderMinutes = values[defaultIndex].takeIf { it >= 0 }
        binding.dropdownReminder.setText(entries[defaultIndex], false)
    }

    private fun setupListeners() {
        binding.buttonStart.setOnClickListener { showDatePicker(true) }
        binding.buttonEnd.setOnClickListener { showDatePicker(false) }
        binding.switchAllDay.setOnCheckedChangeListener { _, isChecked ->
            isAllDay = isChecked
            if (isChecked) {
                val startDate = Instant.ofEpochMilli(startMillis).atZone(zoneId).toLocalDate()
                startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                endMillis = startMillis + ONE_DAY
            }
            updateDateButtons()
        }
        binding.dropdownReminder.setOnItemClickListener { _, _, position, _ ->
            val values = resources.getIntArray(R.array.reminder_values)
            reminderMinutes = values[position].takeIf { it >= 0 }
        }
        binding.buttonSave.setOnClickListener { saveEvent() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
    }

    private fun loadEvent(id: Long) {
        lifecycleScope.launch {
            val event = repository.getEvent(id) ?: return@launch
            binding.inputTitle.setText(event.title)
            binding.inputLocation.setText(event.location)
            binding.inputDescription.setText(event.description)
            binding.switchAllDay.isChecked = event.allDay
            isAllDay = event.allDay
            startMillis = event.startEpochMillis
            endMillis = event.endEpochMillis
            reminderMinutes = event.reminderMinutesBefore
            val entries = resources.getStringArray(R.array.reminder_entries)
            val values = resources.getIntArray(R.array.reminder_values)
            val index = values.indexOf(reminderMinutes ?: -1)
            if (index >= 0) {
                binding.dropdownReminder.setText(entries[index], false)
            } else {
                binding.dropdownReminder.setText(entries.first(), false)
            }
            updateDateButtons()
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val selection = if (isStart) startMillis else endMillis
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(selection)
            .build()
        picker.addOnPositiveButtonClickListener { date ->
            val pickedDate = Instant.ofEpochMilli(date).atZone(zoneId).toLocalDate()
            if (isAllDay) {
                val targetMillis = pickedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                if (isStart) {
                    startMillis = targetMillis
                    if (endMillis <= startMillis) endMillis = startMillis + ONE_DAY
                } else {
                    endMillis = targetMillis + ONE_DAY
                    if (endMillis <= startMillis) endMillis = startMillis + ONE_DAY
                }
                updateDateButtons()
            } else {
                showTimePicker(pickedDate.atStartOfDay(zoneId).toInstant().toEpochMilli(), isStart)
            }
        }
        picker.show(supportFragmentManager, if (isStart) "startDate" else "endDate")
    }

    private fun showTimePicker(baseDateMillis: Long, isStart: Boolean) {
        val base = Instant.ofEpochMilli(baseDateMillis).atZone(zoneId).toLocalDate()
        val current = Instant.ofEpochMilli(if (isStart) startMillis else endMillis).atZone(zoneId)
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(current.hour)
            .setMinute(current.minute)
            .build()
        picker.addOnPositiveButtonClickListener {
            val newMillis = base
                .atTime(picker.hour, picker.minute)
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
            if (isStart) {
                startMillis = newMillis
                if (endMillis <= startMillis) {
                    endMillis = startMillis + 60 * 60 * 1000
                }
            } else {
                endMillis = newMillis
                if (endMillis <= startMillis) {
                    endMillis = startMillis + 60 * 60 * 1000
                }
            }
            updateDateButtons()
        }
        picker.show(supportFragmentManager, if (isStart) "startTime" else "endTime")
    }

    private fun updateDateButtons() {
        val startText = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val endText = Instant.ofEpochMilli(endMillis).atZone(zoneId)
        binding.buttonStart.text =
            if (isAllDay) dateFormatter.format(startText) else dateTimeFormatter.format(startText)
        binding.buttonEnd.text =
            if (isAllDay) dateFormatter.format(endText) else dateTimeFormatter.format(endText)
    }

    private fun saveEvent() {
        val title = binding.inputTitle.text?.toString().orEmpty().trim()
        if (title.isEmpty()) {
            binding.inputTitle.error = getString(R.string.hint_event_title)
            return
        }
        if (endMillis <= startMillis) {
            Toast.makeText(this, "结束时间必须晚于开始时间", Toast.LENGTH_SHORT).show()
            return
        }
        val event = CalendarEvent(
            id = eventId ?: 0,
            title = title,
            description = binding.inputDescription.text?.toString(),
            location = binding.inputLocation.text?.toString(),
            startEpochMillis = startMillis,
            endEpochMillis = endMillis,
            reminderMinutesBefore = reminderMinutes,
            allDay = isAllDay
        )

        lifecycleScope.launch {
            val savedId = repository.upsert(event)
            val storedEvent = event.copy(id = if (eventId == null) savedId else event.id)
            ReminderScheduler.cancel(this@EventEditorActivity, storedEvent)
            if (reminderMinutes != null) {
                ReminderScheduler.schedule(this@EventEditorActivity, storedEvent)
            }
            setResult(RESULT_OK, Intent().putExtra(EXTRA_EVENT_ID, storedEvent.id))
            finish()
        }
    }

    private fun confirmDelete() {
        val id = eventId ?: return
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.dialog_delete_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                lifecycleScope.launch {
                    val event = repository.getEvent(id) ?: return@launch
                    ReminderScheduler.cancel(this@EventEditorActivity, event)
                    repository.delete(event)
                    finish()
                }
            }.show()
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        private const val ONE_DAY = 24 * 60 * 60 * 1000L
    }
}

