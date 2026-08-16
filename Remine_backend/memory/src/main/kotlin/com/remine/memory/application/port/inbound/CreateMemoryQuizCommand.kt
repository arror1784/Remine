package com.remine.memory.application.port.inbound

import com.remine.memory.domain.MemoryQuizQuestion
import java.util.UUID

interface CreateMemoryQuizCommand {
    fun handle(command: In): Out

    data class QuestionIn(
        val question: String,
        val options: List<String>,
        val correctOptionIndex: Int,
    )

    data class In(
        val memoryPhotoId: UUID,
        val questions: List<QuestionIn>,
        /** The parent the caller is acting for — must match the photo's owner. */
        val ownerUserId: UUID,
    )

    data class Out(
        val questions: List<MemoryQuizQuestion>,
    )
}
