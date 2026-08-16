package com.remine.activity.domain

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class DailyActivityRecommendation(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val statDate: LocalDate,
    val parentMessage: String,
    val childMessage: String,
    val actionType: DailyActivityRecommendationActionType,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)
