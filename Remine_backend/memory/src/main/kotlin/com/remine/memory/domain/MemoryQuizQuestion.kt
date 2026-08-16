package com.remine.memory.domain

import java.time.Instant
import java.util.UUID

data class MemoryQuizQuestion(
    val id: UUID = UUID.randomUUID(),
    val memoryPhotoId: UUID,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val sortOrder: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)
