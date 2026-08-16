package com.remine.activity.application.port.inbound

import java.time.LocalDate
import java.util.UUID

interface SyncDailyActivityCommand {
    fun handle(command: In): Out

    data class In(
        val userId: UUID,
        val entries: List<Entry>,
    ) {
        data class Entry(
            val statDate: LocalDate,
            val sleepMinutes: Int,
            val steps: Int,
            val outingCount: Int,
            val socialContactCount: Int,
        )
    }

    data class Out(
        val savedCount: Int,
    )
}
