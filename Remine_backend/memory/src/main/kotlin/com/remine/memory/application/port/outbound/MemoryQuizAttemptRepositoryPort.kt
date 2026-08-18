package com.remine.memory.application.port.outbound

import com.remine.memory.domain.MemoryQuizAttempt
import java.time.Instant
import java.util.UUID

interface MemoryQuizAttemptRepositoryPort {
    fun save(attempt: MemoryQuizAttempt): MemoryQuizAttempt
    fun existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId: UUID, since: Instant): Boolean
    fun findAttemptedPhotoIds(memoryPhotoIds: Collection<UUID>): Set<UUID>

    /** Used by the demo-reset utility (see app-api's DemoResetService) when it deletes a demo photo. */
    fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID)
}
