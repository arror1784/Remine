package com.remine.call.application.port.inbound

import java.time.LocalDate
import java.util.UUID

interface GetCallStatsQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
        val sinceMonthStart: LocalDate,
    )

    data class Out(
        val count: Int,
        val totalDurationSeconds: Long,
    )
}
