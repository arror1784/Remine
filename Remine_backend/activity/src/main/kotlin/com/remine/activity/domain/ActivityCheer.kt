package com.remine.activity.domain

import java.time.Instant
import java.util.UUID

data class ActivityCheer(
    val id: UUID = UUID.randomUUID(),
    val checklistItemId: UUID,
    val senderUserId: UUID,
    val sentAt: Instant,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null,
)
