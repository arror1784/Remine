package com.remine.memory.domain

import java.time.Instant
import java.util.UUID

data class MemoryQuizAttempt(
    val id: UUID = UUID.randomUUID(),
    val memoryPhotoId: UUID,
    val respondentUserId: UUID,
    val correctCount: Int,
    val totalCount: Int,
    val completedAt: Instant = Instant.now(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)
