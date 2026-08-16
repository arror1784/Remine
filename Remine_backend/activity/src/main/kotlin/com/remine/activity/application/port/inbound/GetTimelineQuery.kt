package com.remine.activity.application.port.inbound

import com.remine.activity.domain.ActivityTimelineEvent
import java.time.LocalDate
import java.util.UUID

interface GetTimelineQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
        val statDate: LocalDate,
    )

    data class Out(
        val events: List<ActivityTimelineEvent>,
    )
}
