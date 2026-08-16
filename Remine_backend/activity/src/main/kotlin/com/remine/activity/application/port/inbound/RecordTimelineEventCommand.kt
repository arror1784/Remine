package com.remine.activity.application.port.inbound

import com.remine.activity.domain.ActivityTimelineEvent
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

interface RecordTimelineEventCommand {
    fun handle(command: In): Out

    data class In(
        val userId: UUID,
        val statDate: LocalDate,
        val occurredAt: Instant,
        val label: String,
        val colorHint: String?,
    )

    data class Out(
        val entity: ActivityTimelineEvent,
    )
}
