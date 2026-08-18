package com.remine.activity.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface ActivityTimelineEventJpaRepository : JpaRepository<ActivityTimelineEventJpaEntity, UUID> {
    fun findByUserIdAndStatDateOrderByOccurredAtAsc(
        userId: UUID,
        statDate: LocalDate,
    ): List<ActivityTimelineEventJpaEntity>
    fun findAllByUserId(userId: UUID): List<ActivityTimelineEventJpaEntity>
}
