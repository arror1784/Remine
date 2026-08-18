package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.DailyActivityStat
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class DailyActivityStatRepositoryAdapter(
    private val jpaRepository: DailyActivityStatJpaRepository,
) : DailyActivityStatRepositoryPort {

    override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityStat? {
        return jpaRepository.findByUserIdAndStatDate(userId, statDate)?.toDomain()
    }

    override fun findByUserIdAndStatDateIn(userId: UUID, statDates: Collection<LocalDate>): List<DailyActivityStat> {
        return jpaRepository.findByUserIdAndStatDateIn(userId, statDates).map { it.toDomain() }
    }

    override fun findByUserIdAndStatDateBetween(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<DailyActivityStat> {
        return jpaRepository.findByUserIdAndStatDateBetweenOrderByStatDateAsc(userId, startDate, endDate)
            .map { it.toDomain() }
    }

    override fun save(stat: DailyActivityStat): DailyActivityStat {
        val entity = DailyActivityStatJpaEntity.fromDomain(stat)
        return jpaRepository.save(entity).toDomain()
    }

    override fun saveAll(stats: Collection<DailyActivityStat>): List<DailyActivityStat> {
        val entities = stats.map { DailyActivityStatJpaEntity.fromDomain(it) }
        return jpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun deleteAllByUserId(userId: UUID) {
        val entities = jpaRepository.findAllByUserId(userId)
        if (entities.isNotEmpty()) {
            entities.forEach { it.softDelete() }
            jpaRepository.saveAll(entities)
        }
    }
}
