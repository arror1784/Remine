package com.remine.activity.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface ActivityChecklistItemJpaRepository : JpaRepository<ActivityChecklistItemJpaEntity, UUID> {
    fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): List<ActivityChecklistItemJpaEntity>
    fun findAllByUserId(userId: UUID): List<ActivityChecklistItemJpaEntity>
}
