package com.remine.memory.adapter.presentation.web

import com.remine.memory.domain.MemoryPhoto
import java.time.Instant
import java.util.UUID

data class MemoryPhotoResponse(
    val id: UUID,
    val ownerUserId: UUID,
    val uploadedByUserId: UUID,
    val title: String,
    val photoUrl: String,
    val memoryLabel: String,
    val status: String,
    val attempted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(domain: MemoryPhoto, attempted: Boolean = false): MemoryPhotoResponse =
            MemoryPhotoResponse(
                id = domain.id,
                ownerUserId = domain.ownerUserId,
                uploadedByUserId = domain.uploadedByUserId,
                title = domain.title,
                photoUrl = domain.photoUrl,
                memoryLabel = domain.memoryLabel,
                status = domain.status.name,
                attempted = attempted,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
            )
    }
}
