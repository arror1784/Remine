package com.remine.message.domain

import java.time.Instant
import java.util.UUID

data class QuickReply(
    val id: UUID = UUID.randomUUID(),
    val role: String,
    val label: String,
    val sortOrder: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
