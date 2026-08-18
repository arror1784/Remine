package com.remine.memory.adapter.infrastructure.jpa

import com.remine.memory.application.port.outbound.MemoryQuizAttemptRepositoryPort
import com.remine.memory.domain.MemoryQuizAttempt
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MemoryQuizAttemptRepositoryAdapter(
    private val jpaRepository: MemoryQuizAttemptJpaRepository,
) : MemoryQuizAttemptRepositoryPort {

    override fun save(attempt: MemoryQuizAttempt): MemoryQuizAttempt {
        val entity = MemoryQuizAttemptJpaEntity.from(attempt)
        return jpaRepository.save(entity).toDomain()
    }

    override fun existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId: UUID, since: Instant): Boolean =
        jpaRepository.existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId, since)

    override fun findAttemptedPhotoIds(memoryPhotoIds: Collection<UUID>): Set<UUID> =
        if (memoryPhotoIds.isEmpty()) emptySet() else jpaRepository.findDistinctMemoryPhotoIdByMemoryPhotoIdIn(memoryPhotoIds)

    override fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID) {
        val entities = jpaRepository.findAllByMemoryPhotoId(memoryPhotoId)
        if (entities.isNotEmpty()) {
            entities.forEach { it.softDelete() }
            jpaRepository.saveAll(entities)
        }
    }
}
