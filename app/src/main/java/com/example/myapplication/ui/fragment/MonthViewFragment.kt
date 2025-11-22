package com.example.myapplication.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.CalendarApplication
import com.example.myapplication.EventEditorActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMonthViewBinding
import com.example.myapplication.ui.adapter.EventListAdapter
import com.example.myapplication.ui.viewmodel.CalendarViewModel
import com.example.myapplication.util.LunarUtils
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

class MonthViewFragment : Fragment() {

    private var _binding: FragmentMonthViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by activityViewModels {
        CalendarViewModel.Factory((requireActivity().application as CalendarApplication).repository)
    }

    private val eventAdapter by lazy {
        EventListAdapter { event ->
            val intent = Intent(requireContext(), EventEditorActivity::class.java).apply {
                putExtra(EventEditorActivity.EXTRA_EVENT_ID, event.id)
            }
            startActivity(intent)
        }
    }

    private val zoneId = ZoneId.systemDefault()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonthViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.monthEventList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }

        binding.monthCalendar.setOnDateChangeListener { _, year, month, day ->
            viewModel.selectDate(LocalDate.of(year, month + 1, day))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDate.collect { date ->
                        binding.monthTitle.text =
                            getString(
                                com.example.myapplication.R.string.month_title_format,
                                date.year,
                                date.monthValue
                            )
                        // 显示农历
                        val lunarText = LunarUtils.getLunarText(date)
                        binding.monthLunarText.text = lunarText
                        binding.monthLunarText.visibility = 
                            if (lunarText.isNotEmpty()) View.VISIBLE else View.GONE
                        
                        val millis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                        if (binding.monthCalendar.date != millis) {
                            binding.monthCalendar.date = millis
                        }
                    }
                }
                launch {
                    viewModel.dayEvents.collect { events ->
                        eventAdapter.submitList(events)
                        binding.monthEmptyView.visibility =
                            if (events.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

