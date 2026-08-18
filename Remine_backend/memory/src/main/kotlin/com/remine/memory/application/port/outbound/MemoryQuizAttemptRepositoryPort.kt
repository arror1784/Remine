package com.remine.memory.application.port.outbound

import com.remine.memory.domain.MemoryQuizAttempt
import java.time.Instant
import java.util.UUID

interface MemoryQuizAttemptRepositoryPort {
    fun save(attempt: MemoryQuizAttempt): MemoryQuizAttempt
    fun existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId: UUID, since: Instant): Boolean
    fun findAttemptedPhotoIds(memoryPhotoIds: Collection<UUID>): Set<UUID>
}
