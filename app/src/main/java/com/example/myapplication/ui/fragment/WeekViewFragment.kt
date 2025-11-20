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
import com.example.myapplication.databinding.FragmentWeekViewBinding
import com.example.myapplication.ui.adapter.WeekDayAdapter
import com.example.myapplication.ui.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch

class WeekViewFragment : Fragment() {

    private var _binding: FragmentWeekViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by activityViewModels {
        CalendarViewModel.Factory((requireActivity().application as CalendarApplication).repository)
    }

    private val weekDayAdapter by lazy {
        WeekDayAdapter { eventId ->
            val intent = Intent(requireContext(), EventEditorActivity::class.java).apply {
                putExtra(EventEditorActivity.EXTRA_EVENT_ID, eventId)
            }
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeekViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.weekList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = weekDayAdapter
        }

        binding.weekPrev.setOnClickListener { viewModel.moveWeek(-1) }
        binding.weekNext.setOnClickListener { viewModel.moveWeek(1) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.weekDaysWithEvents.collect { days ->
                        if (days.isNotEmpty()) {
                            val start = days.first().date
                            val end = days.last().date
                            binding.weekRange.text = getString(
                                R.string.label_week_range,
                                getString(R.string.week_day_format, start.monthValue, start.dayOfMonth),
                                getString(R.string.week_day_format, end.monthValue, end.dayOfMonth)
                            )
                        }
                        weekDayAdapter.submitList(days)
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


