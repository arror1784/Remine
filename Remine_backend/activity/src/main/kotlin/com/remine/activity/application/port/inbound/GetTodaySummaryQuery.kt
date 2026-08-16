package com.remine.activity.application.port.inbound

import com.remine.activity.domain.DailyActivityStat
import java.util.UUID

interface GetTodaySummaryQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
    )

    data class Out(
        val stat: DailyActivityStat?,
        val sleepPercent: Int,
        val stepsPercent: Int,
        val outingPercent: Int,
        val socialPercent: Int,
    )
}
