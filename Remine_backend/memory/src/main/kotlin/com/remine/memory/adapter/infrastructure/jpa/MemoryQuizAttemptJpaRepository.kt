package com.remine.memory.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface MemoryQuizAttemptJpaRepository : JpaRepository<MemoryQuizAttemptJpaEntity, UUID> {
    fun existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId: UUID, completedAt: Instant): Boolean

    @Query("SELECT DISTINCT a.memoryPhotoId FROM MemoryQuizAttemptJpaEntity a WHERE a.memoryPhotoId IN :memoryPhotoIds")
    fun findDistinctMemoryPhotoIdByMemoryPhotoIdIn(memoryPhotoIds: Collection<UUID>): Set<UUID>
}
