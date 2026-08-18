package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.application.port.outbound.ActivityChecklistItemRepositoryPort
import com.remine.activity.domain.ActivityChecklistItem
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class ActivityChecklistItemRepositoryAdapter(
    private val jpaRepository: ActivityChecklistItemJpaRepository,
) : ActivityChecklistItemRepositoryPort {

    override fun findById(id: UUID): ActivityChecklistItem? {
        return jpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): List<ActivityChecklistItem> {
        return jpaRepository.findByUserIdAndStatDate(userId, statDate).map { it.toDomain() }
    }

    override fun save(item: ActivityChecklistItem): ActivityChecklistItem {
        val entity = ActivityChecklistItemJpaEntity.fromDomain(item)
        return jpaRepository.save(entity).toDomain()
    }

    override fun saveAll(items: Collection<ActivityChecklistItem>): List<ActivityChecklistItem> {
        val entities = items.map { ActivityChecklistItemJpaEntity.fromDomain(it) }
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
