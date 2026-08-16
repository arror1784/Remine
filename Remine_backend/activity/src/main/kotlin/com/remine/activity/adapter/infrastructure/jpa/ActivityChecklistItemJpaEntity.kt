package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.domain.ActivityChecklistItem
import com.remine.common.persistence.BaseOrmEntity
import org.hibernate.annotations.Where
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "activity_checklist_item")
@Where(clause = "deleted_at IS NULL")
class ActivityChecklistItemJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    var userId: UUID,

    @Column(name = "stat_date", nullable = false)
    var statDate: LocalDate,

    @Column(name = "type", length = 20, nullable = false)
    var type: String,

    @Column(name = "done", nullable = false)
    var done: Boolean = false,

    @Column(name = "completed_at")
    var completedAt: Instant? = null,

    @Column(name = "note", length = 200)
    var note: String? = null,
) : BaseOrmEntity(id) {

    fun toDomain(): ActivityChecklistItem = ActivityChecklistItem(
        id = id,
        userId = userId,
        statDate = statDate,
        type = type,
        done = done,
        completedAt = completedAt,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

    companion object {
        fun fromDomain(domain: ActivityChecklistItem): ActivityChecklistItemJpaEntity =
            ActivityChecklistItemJpaEntity(
                id = domain.id,
                userId = domain.userId,
                statDate = domain.statDate,
                type = domain.type,
                done = domain.done,
                completedAt = domain.completedAt,
                note = domain.note,
            ).apply {
                if (domain.deletedAt != null) {
                    softDelete()
                }
            }
    }
}
