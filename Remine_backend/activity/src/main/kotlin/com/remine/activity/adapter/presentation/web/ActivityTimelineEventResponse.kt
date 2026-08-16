package com.remine.activity.adapter.presentation.web

import com.remine.activity.domain.ActivityTimelineEvent
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ActivityTimelineEventResponse(
    val id: UUID,
    val userId: UUID,
    val statDate: LocalDate,
    val occurredAt: Instant,
    val label: String,
    val colorHint: String?,
) {
    companion object {
        fun from(event: ActivityTimelineEvent): ActivityTimelineEventResponse =
            ActivityTimelineEventResponse(
                id = event.id,
                userId = event.userId,
                statDate = event.statDate,
                occurredAt = event.occurredAt,
                label = event.label,
                colorHint = event.colorHint,
            )
    }
}
