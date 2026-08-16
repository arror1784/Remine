package com.remine.memory.adapter.presentation.web

import com.remine.memory.domain.MemoryQuizDraftQuestion
import java.time.Instant
import java.util.UUID

data class MemoryQuizDraftQuestionResponse(
    val id: UUID,
    val memoryPhotoId: UUID,
    val question: String,
    val sortOrder: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(domain: MemoryQuizDraftQuestion): MemoryQuizDraftQuestionResponse =
            MemoryQuizDraftQuestionResponse(
                id = domain.id,
                memoryPhotoId = domain.memoryPhotoId,
                question = domain.question,
                sortOrder = domain.sortOrder,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
            )
    }
}
