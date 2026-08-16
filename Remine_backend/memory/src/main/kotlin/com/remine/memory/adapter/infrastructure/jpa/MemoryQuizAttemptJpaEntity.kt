package com.remine.memory.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.memory.domain.MemoryQuizAttempt
import org.hibernate.annotations.Where
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "memory_quiz_attempt")
@Where(clause = "deleted_at IS NULL")
class MemoryQuizAttemptJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "memory_photo_id", columnDefinition = "uuid", nullable = false)
    val memoryPhotoId: UUID,

    @Column(name = "respondent_user_id", columnDefinition = "uuid", nullable = false)
    val respondentUserId: UUID,

    @Column(name = "correct_count", nullable = false)
    var correctCount: Int,

    @Column(name = "total_count", nullable = false)
    var totalCount: Int,

    @Column(name = "completed_at", nullable = false)
    var completedAt: Instant,
) : BaseOrmEntity(id) {

    fun toDomain(): MemoryQuizAttempt =
        MemoryQuizAttempt(
            id = id,
            memoryPhotoId = memoryPhotoId,
            respondentUserId = respondentUserId,
            correctCount = correctCount,
            totalCount = totalCount,
            completedAt = completedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
        )

    companion object {
        fun from(domain: MemoryQuizAttempt): MemoryQuizAttemptJpaEntity {
            return MemoryQuizAttemptJpaEntity(
                id = domain.id,
                memoryPhotoId = domain.memoryPhotoId,
                respondentUserId = domain.respondentUserId,
                correctCount = domain.correctCount,
                totalCount = domain.totalCount,
                completedAt = domain.completedAt,
            )
        }
    }
}
