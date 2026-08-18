package com.remine.memory.application.port.outbound

import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import java.time.Instant
import java.util.UUID

interface MemoryPhotoRepositoryPort {
    fun save(photo: MemoryPhoto): MemoryPhoto
    fun findById(id: UUID): MemoryPhoto?
    fun findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId: UUID): List<MemoryPhoto>
    fun countByOwnerUserId(ownerUserId: UUID): Int
    fun countByOwnerUserIdAndStatus(ownerUserId: UUID, status: MemoryPhotoStatus): Int
    fun countByOwnerUserIdAndCreatedAtGreaterThanEqual(ownerUserId: UUID, startOfMonth: Instant): Int
    fun findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId: UUID, status: MemoryPhotoStatus): List<MemoryPhoto>

    /** Used by the demo-reset utility (see app-api's DemoResetService) to wipe a demo account's photos. */
    fun deleteAllByOwnerUserId(ownerUserId: UUID)
}
