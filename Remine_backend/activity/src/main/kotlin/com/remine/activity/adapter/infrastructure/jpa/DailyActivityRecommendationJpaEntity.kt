package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.domain.DailyActivityRecommendation
import com.remine.activity.domain.DailyActivityRecommendationActionType
import com.remine.common.persistence.BaseOrmEntity
import org.hibernate.annotations.Where
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "daily_activity_recommendation")
@Where(clause = "deleted_at IS NULL")
class DailyActivityRecommendationJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    var userId: UUID,

    @Column(name = "stat_date", nullable = false)
    var statDate: LocalDate,

    @Column(name = "parent_message", length = 500, nullable = false)
    var parentMessage: String,

    @Column(name = "child_message", length = 500, nullable = false)
    var childMessage: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 20, nullable = false)
    var actionType: DailyActivityRecommendationActionType,
) : BaseOrmEntity(id) {

    fun toDomain(): DailyActivityRecommendation = DailyActivityRecommendation(
        id = id,
        userId = userId,
        statDate = statDate,
        parentMessage = parentMessage,
        childMessage = childMessage,
        actionType = actionType,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

    companion object {
        fun fromDomain(domain: DailyActivityRecommendation): DailyActivityRecommendationJpaEntity =
            DailyActivityRecommendationJpaEntity(
                id = domain.id,
                userId = domain.userId,
                statDate = domain.statDate,
                parentMessage = domain.parentMessage,
                childMessage = domain.childMessage,
                actionType = domain.actionType,
            ).apply {
                if (domain.deletedAt != null) {
                    softDelete()
                }
            }
    }
}
