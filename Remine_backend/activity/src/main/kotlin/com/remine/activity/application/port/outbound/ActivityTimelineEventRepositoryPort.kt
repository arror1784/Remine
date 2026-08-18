package com.remine.activity.application.port.outbound

import com.remine.activity.domain.ActivityTimelineEvent
import java.time.LocalDate
import java.util.UUID

interface ActivityTimelineEventRepositoryPort {
    fun findByUserIdAndStatDateOrderByOccurredAtAsc(userId: UUID, statDate: LocalDate): List<ActivityTimelineEvent>
    fun save(event: ActivityTimelineEvent): ActivityTimelineEvent

    /** Used by the demo-reset utility (see app-api's DemoResetService) to wipe a demo account's history. */
    fun deleteAllByUserId(userId: UUID)
}
