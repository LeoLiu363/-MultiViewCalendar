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
import com.example.myapplication.databinding.FragmentDayViewBinding
import com.example.myapplication.ui.adapter.EventListAdapter
import com.example.myapplication.ui.viewmodel.CalendarViewModel
import com.example.myapplication.util.TimeUtils
import kotlinx.coroutines.launch

class DayViewFragment : Fragment() {

    private var _binding: FragmentDayViewBinding? = null
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDayViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dayList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }

        binding.dayPrev.setOnClickListener { viewModel.moveDay(-1) }
        binding.dayNext.setOnClickListener { viewModel.moveDay(1) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDate.collect { date ->
                        binding.dayTitle.text = TimeUtils.dateLabel(date)
                    }
                }
                launch {
                    viewModel.dayEvents.collect { events ->
                        eventAdapter.submitList(events)
                        binding.dayEmpty.visibility =
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


