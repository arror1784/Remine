package com.remine.activity.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface DailyActivityStatJpaRepository : JpaRepository<DailyActivityStatJpaEntity, UUID> {
    fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityStatJpaEntity?
    fun findByUserIdAndStatDateIn(userId: UUID, statDates: Collection<LocalDate>): List<DailyActivityStatJpaEntity>
    fun findByUserIdAndStatDateBetweenOrderByStatDateAsc(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<DailyActivityStatJpaEntity>
    fun findAllByUserId(userId: UUID): List<DailyActivityStatJpaEntity>
}
