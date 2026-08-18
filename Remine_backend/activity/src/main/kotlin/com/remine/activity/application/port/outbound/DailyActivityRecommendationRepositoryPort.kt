package com.remine.activity.application.port.outbound

import com.remine.activity.domain.DailyActivityRecommendation
import java.time.LocalDate
import java.util.UUID

interface DailyActivityRecommendationRepositoryPort {
    fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityRecommendation?
    fun save(recommendation: DailyActivityRecommendation): DailyActivityRecommendation

    /**
     * Hard-deletes the cached recommendation for this user+date, if any, so the next read
     * regenerates it from fresh stats. A real DELETE rather than this project's usual soft
     * delete on purpose — this row is a pure derived cache with no audit value, and a soft
     * delete would leave a "deleted" row that still collides with the (userId, statDate)
     * unique index on the next insert.
     */
    fun deleteByUserIdAndStatDate(userId: UUID, statDate: LocalDate)
}
