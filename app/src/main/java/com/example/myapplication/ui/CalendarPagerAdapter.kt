package com.example.myapplication.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.ui.fragment.DayViewFragment
import com.example.myapplication.ui.fragment.MonthViewFragment
import com.example.myapplication.ui.fragment.WeekViewFragment

class CalendarPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MonthViewFragment()
        1 -> WeekViewFragment()
        else -> DayViewFragment()
    }
}


