package com.remine.memory.application.port.inbound

import com.remine.memory.domain.MemoryQuizDraftQuestion
import java.util.UUID

interface GenerateMemoryQuizQuestionsCommand {
    fun handle(command: In): Out

    data class In(
        val memoryPhotoId: UUID,
        /** The parent the caller is acting for — must match the photo's owner. */
        val ownerUserId: UUID,
        val count: Int = 3,
    )

    data class Out(
        val questions: List<MemoryQuizDraftQuestion>,
    )
}
