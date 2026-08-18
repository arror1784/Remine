package com.remine.memory.application.port.inbound

import com.remine.memory.domain.MemoryPhoto
import java.util.UUID

interface GetMemoryGalleryQuery {
    fun handle(query: In): Out

    data class In(
        val ownerUserId: UUID,
    )

    data class Out(
        val items: List<MemoryPhoto>,
        val attemptedPhotoIds: Set<UUID> = emptySet(),
    )
}
