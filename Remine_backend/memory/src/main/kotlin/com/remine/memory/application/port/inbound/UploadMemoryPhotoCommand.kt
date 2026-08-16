package com.remine.memory.application.port.inbound

import com.remine.memory.domain.MemoryPhoto
import java.util.UUID

interface UploadMemoryPhotoCommand {
    fun handle(command: In): Out

    data class In(
        val uploadedByUserId: UUID,
        val ownerUserId: UUID,
        val title: String,
        val photoUrl: String,
        val memoryLabel: String,
    )

    data class Out(
        val entity: MemoryPhoto,
    )
}
