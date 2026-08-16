package com.remine.activity.domain

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ActivityChecklistItem(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val statDate: LocalDate,
    val type: String,
    val done: Boolean = false,
    val completedAt: Instant? = null,
    val note: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)
