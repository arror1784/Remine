package com.remine.memory.application.port.outbound

import com.remine.memory.domain.MemoryQuizQuestion
import java.util.UUID

interface MemoryQuizQuestionRepositoryPort {
    fun saveAll(questions: List<MemoryQuizQuestion>): List<MemoryQuizQuestion>
    fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizQuestion>
}
