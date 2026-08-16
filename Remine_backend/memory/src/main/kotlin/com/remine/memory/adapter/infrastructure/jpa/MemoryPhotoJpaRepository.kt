package com.remine.memory.adapter.infrastructure.jpa

import com.remine.memory.domain.MemoryPhotoStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface MemoryPhotoJpaRepository : JpaRepository<MemoryPhotoJpaEntity, UUID> {
    fun findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId: UUID): List<MemoryPhotoJpaEntity>
    fun countByOwnerUserId(ownerUserId: UUID): Int
    fun countByOwnerUserIdAndStatus(ownerUserId: UUID, status: MemoryPhotoStatus): Int
    fun countByOwnerUserIdAndCreatedAtGreaterThanEqual(ownerUserId: UUID, createdAt: Instant): Int
    fun findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId: UUID, status: MemoryPhotoStatus): List<MemoryPhotoJpaEntity>
}
