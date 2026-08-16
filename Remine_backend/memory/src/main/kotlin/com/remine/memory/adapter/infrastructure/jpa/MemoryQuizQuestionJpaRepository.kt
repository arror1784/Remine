package com.remine.memory.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MemoryQuizQuestionJpaRepository : JpaRepository<MemoryQuizQuestionJpaEntity, UUID> {
    fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizQuestionJpaEntity>
}
