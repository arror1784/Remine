package com.remine.memory.domain

import java.time.Instant
import java.util.UUID

data class MemoryPhoto(
    val id: UUID = UUID.randomUUID(),
    val ownerUserId: UUID,
    val uploadedByUserId: UUID,
    val title: String,
    val photoUrl: String,
    val memoryLabel: String,
    val status: MemoryPhotoStatus = MemoryPhotoStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
) {
    fun withStatus(newStatus: MemoryPhotoStatus): MemoryPhoto =
        copy(status = newStatus, updatedAt = Instant.now())
}
