package com.remine.activity.adapter.presentation.web

data class TodaySummaryResponse(
    val stat: DailyActivityStatResponse?,
    val sleepPercent: Int,
    val stepsPercent: Int,
    val outingPercent: Int,
    val socialPercent: Int,
)
