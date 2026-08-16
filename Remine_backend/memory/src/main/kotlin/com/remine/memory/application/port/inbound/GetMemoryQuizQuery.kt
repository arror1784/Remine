package com.remine.memory.application.port.inbound

import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.QuestionView
import java.util.UUID

interface GetMemoryQuizQuery {
    fun handle(query: In): Out

    data class In(
        val memoryPhotoId: UUID,
        /** The parent the caller is acting for — must match the photo's owner. */
        val ownerUserId: UUID,
    )

    data class Out(
        val photo: MemoryPhoto,
        val questions: List<QuestionView>,
    )
}
