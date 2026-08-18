package com.remine.memory.adapter.infrastructure.jpa

import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MemoryPhotoRepositoryAdapter(
    private val jpaRepository: MemoryPhotoJpaRepository,
) : MemoryPhotoRepositoryPort {

    override fun save(photo: MemoryPhoto): MemoryPhoto {
        val existing = jpaRepository.findByIdOrNull(photo.id)
        val entityToSave = if (existing != null) {
            existing.updateFrom(photo)
            existing
        } else {
            MemoryPhotoJpaEntity.from(photo)
        }
        return jpaRepository.save(entityToSave).toDomain()
    }

    override fun findById(id: UUID): MemoryPhoto? =
        jpaRepository.findByIdOrNull(id)?.toDomain()

    override fun findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId: UUID): List<MemoryPhoto> =
        jpaRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId).map { it.toDomain() }

    override fun countByOwnerUserId(ownerUserId: UUID): Int =
        jpaRepository.countByOwnerUserId(ownerUserId)

    override fun countByOwnerUserIdAndStatus(ownerUserId: UUID, status: MemoryPhotoStatus): Int =
        jpaRepository.countByOwnerUserIdAndStatus(ownerUserId, status)

    override fun countByOwnerUserIdAndCreatedAtGreaterThanEqual(ownerUserId: UUID, startOfMonth: Instant): Int =
        jpaRepository.countByOwnerUserIdAndCreatedAtGreaterThanEqual(ownerUserId, startOfMonth)

    override fun findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId: UUID, status: MemoryPhotoStatus): List<MemoryPhoto> =
        jpaRepository.findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId, status).map { it.toDomain() }

    override fun deleteAllByOwnerUserId(ownerUserId: UUID) {
        val entities = jpaRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId)
        if (entities.isNotEmpty()) {
            entities.forEach { it.softDelete() }
            jpaRepository.saveAll(entities)
        }
    }
}
