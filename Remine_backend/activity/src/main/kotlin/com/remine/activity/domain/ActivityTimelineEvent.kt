package com.remine.activity.domain

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ActivityTimelineEvent(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val statDate: LocalDate,
    val occurredAt: Instant,
    val label: String,
    val colorHint: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)
