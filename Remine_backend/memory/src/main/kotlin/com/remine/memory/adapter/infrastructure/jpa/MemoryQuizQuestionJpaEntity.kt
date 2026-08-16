package com.remine.memory.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.memory.domain.MemoryQuizQuestion
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "memory_quiz_question")
@Where(clause = "deleted_at IS NULL")
class MemoryQuizQuestionJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "memory_photo_id", columnDefinition = "uuid", nullable = false)
    val memoryPhotoId: UUID,

    @Column(name = "question", length = 500, nullable = false)
    var question: String,

    @Convert(converter = StringListJsonConverter::class)
    @Column(name = "options_json", length = 1000, nullable = false)
    var options: List<String>,

    @Column(name = "correct_option_index", nullable = false)
    var correctOptionIndex: Int,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,
) : BaseOrmEntity(id) {

    fun toDomain(): MemoryQuizQuestion =
        MemoryQuizQuestion(
            id = id,
            memoryPhotoId = memoryPhotoId,
            question = question,
            options = options,
            correctOptionIndex = correctOptionIndex,
            sortOrder = sortOrder,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
        )

    companion object {
        fun from(domain: MemoryQuizQuestion): MemoryQuizQuestionJpaEntity {
            return MemoryQuizQuestionJpaEntity(
                id = domain.id,
                memoryPhotoId = domain.memoryPhotoId,
                question = domain.question,
                options = domain.options,
                correctOptionIndex = domain.correctOptionIndex,
                sortOrder = domain.sortOrder,
            )
        }
    }
}
