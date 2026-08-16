package com.remine.memory.adapter.infrastructure.jpa

import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryQuizQuestion
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MemoryQuizQuestionRepositoryAdapter(
    private val jpaRepository: MemoryQuizQuestionJpaRepository,
) : MemoryQuizQuestionRepositoryPort {

    override fun saveAll(questions: List<MemoryQuizQuestion>): List<MemoryQuizQuestion> {
        val entities = questions.map { MemoryQuizQuestionJpaEntity.from(it) }
        return jpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizQuestion> =
        jpaRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId).map { it.toDomain() }
}
