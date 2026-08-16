package com.remine.memory.adapter.presentation.web

import com.remine.memory.domain.MemoryQuizQuestion
import java.util.UUID

data class MemoryQuizQuestionResponse(
    val id: UUID,
    val memoryPhotoId: UUID,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val sortOrder: Int,
) {
    companion object {
        fun from(domain: MemoryQuizQuestion): MemoryQuizQuestionResponse =
            MemoryQuizQuestionResponse(
                id = domain.id,
                memoryPhotoId = domain.memoryPhotoId,
                question = domain.question,
                options = domain.options,
                correctOptionIndex = domain.correctOptionIndex,
                sortOrder = domain.sortOrder,
            )
    }
}
