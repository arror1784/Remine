package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.domain.DailyActivityStat
import com.remine.common.persistence.BaseOrmEntity
import org.hibernate.annotations.Where
import java.time.LocalDate
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "daily_activity_stat")
@Where(clause = "deleted_at IS NULL")
class DailyActivityStatJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    var userId: UUID,

    @Column(name = "stat_date", nullable = false)
    var statDate: LocalDate,

    @Column(name = "sleep_minutes", nullable = false)
    var sleepMinutes: Int = 0,

    @Column(name = "steps", nullable = false)
    var steps: Int = 0,

    @Column(name = "outing_count", nullable = false)
    var outingCount: Int = 0,

    @Column(name = "social_contact_count", nullable = false)
    var socialContactCount: Int = 0,

    @Column(name = "sleep_goal_minutes", nullable = false)
    var sleepGoalMinutes: Int = 480,

    @Column(name = "steps_goal", nullable = false)
    var stepsGoal: Int = 8000,

    @Column(name = "outing_goal", nullable = false)
    var outingGoal: Int = 1,

    @Column(name = "social_goal", nullable = false)
    var socialGoal: Int = 1,
) : BaseOrmEntity(id) {

    fun toDomain(): DailyActivityStat = DailyActivityStat(
        id = id,
        userId = userId,
        statDate = statDate,
        sleepMinutes = sleepMinutes,
        steps = steps,
        outingCount = outingCount,
        socialContactCount = socialContactCount,
        sleepGoalMinutes = sleepGoalMinutes,
        stepsGoal = stepsGoal,
        outingGoal = outingGoal,
        socialGoal = socialGoal,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

    companion object {
        fun fromDomain(domain: DailyActivityStat): DailyActivityStatJpaEntity =
            DailyActivityStatJpaEntity(
                id = domain.id,
                userId = domain.userId,
                statDate = domain.statDate,
                sleepMinutes = domain.sleepMinutes,
                steps = domain.steps,
                outingCount = domain.outingCount,
                socialContactCount = domain.socialContactCount,
                sleepGoalMinutes = domain.sleepGoalMinutes,
                stepsGoal = domain.stepsGoal,
                outingGoal = domain.outingGoal,
                socialGoal = domain.socialGoal,
            ).apply {
                if (domain.deletedAt != null) {
                    softDelete()
                }
            }
    }
}
