package com.remine.memory.application.port.outbound

import com.remine.memory.domain.MemoryQuizDraftQuestion
import java.util.UUID

interface MemoryQuizDraftQuestionRepositoryPort {
    fun saveAll(questions: List<MemoryQuizDraftQuestion>): List<MemoryQuizDraftQuestion>
    fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizDraftQuestion>
    fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID)
}
