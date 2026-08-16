package com.remine.memory.adapter.infrastructure.jpa

import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import com.remine.memory.domain.MemoryQuizDraftQuestion
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MemoryQuizDraftQuestionRepositoryAdapter(
    private val jpaRepository: MemoryQuizDraftQuestionJpaRepository,
) : MemoryQuizDraftQuestionRepositoryPort {

    override fun saveAll(questions: List<MemoryQuizDraftQuestion>): List<MemoryQuizDraftQuestion> {
        val entities = questions.map { MemoryQuizDraftQuestionJpaEntity.from(it) }
        return jpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizDraftQuestion> =
        jpaRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId).map { it.toDomain() }

    override fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID) {
        val entities = jpaRepository.findAllByMemoryPhotoId(memoryPhotoId)
        if (entities.isNotEmpty()) {
            entities.forEach { it.softDelete() }
            jpaRepository.saveAll(entities)
        }
    }
}
