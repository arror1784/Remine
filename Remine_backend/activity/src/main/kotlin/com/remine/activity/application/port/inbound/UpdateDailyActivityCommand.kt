package com.remine.activity.application.port.inbound

import com.remine.activity.domain.DailyActivityStat
import java.time.LocalDate
import java.util.UUID

interface UpdateDailyActivityCommand {
    fun handle(command: In): Out

    data class In(
        val userId: UUID,
        val statDate: LocalDate,
        val sleepMinutes: Int?,
        val steps: Int?,
        val outingCount: Int?,
        val socialContactCount: Int?,
    )

    data class Out(
        val entity: DailyActivityStat,
    )
}
