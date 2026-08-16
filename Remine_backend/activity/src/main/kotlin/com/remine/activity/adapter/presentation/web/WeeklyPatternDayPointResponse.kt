package com.remine.activity.adapter.presentation.web

import java.time.LocalDate

data class WeeklyPatternDayPointResponse(
    val statDate: LocalDate,
    val steps: Int,
    val isToday: Boolean,
)
