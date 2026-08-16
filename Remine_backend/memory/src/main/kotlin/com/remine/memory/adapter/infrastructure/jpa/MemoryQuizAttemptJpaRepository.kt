package com.remine.memory.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface MemoryQuizAttemptJpaRepository : JpaRepository<MemoryQuizAttemptJpaEntity, UUID> {
    fun existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId: UUID, completedAt: Instant): Boolean
}
