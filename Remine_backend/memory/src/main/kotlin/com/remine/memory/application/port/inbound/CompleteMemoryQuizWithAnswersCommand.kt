package com.remine.memory.application.port.inbound

import com.remine.memory.domain.MemoryQuizQuestion
import java.util.UUID

interface CompleteMemoryQuizWithAnswersCommand {
    fun handle(command: In): Out

    data class AnswerIn(
        val draftQuestionId: UUID? = null,
        val question: String? = null,
        val answer: String,
    )

    data class In(
        val memoryPhotoId: UUID,
        /** The parent the caller is acting for — must match the photo's owner. */
        val ownerUserId: UUID,
        val answers: List<AnswerIn>,
    )

    data class Out(
        val questions: List<MemoryQuizQuestion>,
    )
}
