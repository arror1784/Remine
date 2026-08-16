package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.domain.ActivityTimelineEvent
import com.remine.common.persistence.BaseOrmEntity
import org.hibernate.annotations.Where
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "activity_timeline_event")
@Where(clause = "deleted_at IS NULL")
class ActivityTimelineEventJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    var userId: UUID,

    @Column(name = "stat_date", nullable = false)
    var statDate: LocalDate,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant,

    @Column(name = "label", length = 200, nullable = false)
    var label: String,

    @Column(name = "color_hint", length = 16)
    var colorHint: String? = null,
) : BaseOrmEntity(id) {

    fun toDomain(): ActivityTimelineEvent = ActivityTimelineEvent(
        id = id,
        userId = userId,
        statDate = statDate,
        occurredAt = occurredAt,
        label = label,
        colorHint = colorHint,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

    companion object {
        fun fromDomain(domain: ActivityTimelineEvent): ActivityTimelineEventJpaEntity =
            ActivityTimelineEventJpaEntity(
                id = domain.id,
                userId = domain.userId,
                statDate = domain.statDate,
                occurredAt = domain.occurredAt,
                label = domain.label,
                colorHint = domain.colorHint,
            ).apply {
                if (domain.deletedAt != null) {
                    softDelete()
                }
            }
    }
}
