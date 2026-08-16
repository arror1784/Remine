package com.remine.memory.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemoryQuizDraftQuestionJpaRepository : JpaRepository<MemoryQuizDraftQuestionJpaEntity, UUID> {
    fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizDraftQuestionJpaEntity>
    fun findAllByMemoryPhotoId(memoryPhotoId: UUID): List<MemoryQuizDraftQuestionJpaEntity>
}
