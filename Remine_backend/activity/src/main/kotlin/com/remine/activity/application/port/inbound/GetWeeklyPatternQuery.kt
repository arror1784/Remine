package com.remine.activity.application.port.inbound

import java.time.LocalDate
import java.util.UUID

interface GetWeeklyPatternQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
    )

    data class DayPoint(
        val statDate: LocalDate,
        val steps: Int,
        val isToday: Boolean,
    )

    data class Out(
        val days: List<DayPoint>,
    )
}
