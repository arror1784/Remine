package com.remine.activity.domain

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class DailyActivityStat(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val statDate: LocalDate,
    val sleepMinutes: Int = 0,
    val steps: Int = 0,
    val outingCount: Int = 0,
    val socialContactCount: Int = 0,
    val sleepGoalMinutes: Int = 480,
    val stepsGoal: Int = 8000,
    val outingGoal: Int = 1,
    val socialGoal: Int = 1,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)
