package com.remine.memory.application.port.inbound

import com.remine.memory.domain.MemoryQuizQuestion
import java.util.UUID

interface GenerateMemoryQuizCommand {
    fun handle(command: In): Out

    data class In(
        val memoryPhotoId: UUID,
        /** The parent the caller is acting for — must match the photo's owner. */
        val ownerUserId: UUID,
    )

    data class Out(
        val questions: List<MemoryQuizQuestion>,
    )
}
