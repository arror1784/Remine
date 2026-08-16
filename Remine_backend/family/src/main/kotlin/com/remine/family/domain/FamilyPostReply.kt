package com.remine.family.domain

import java.time.Instant
import java.util.UUID

data class FamilyPostReply(
    val id: UUID = UUID.randomUUID(),
    val postId: UUID,
    val authorUserId: UUID,
    val body: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
