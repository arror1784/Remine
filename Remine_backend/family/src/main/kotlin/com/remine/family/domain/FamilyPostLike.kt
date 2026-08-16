package com.remine.family.domain

import java.time.Instant
import java.util.UUID

data class FamilyPostLike(
    val id: UUID = UUID.randomUUID(),
    val postId: UUID,
    val userId: UUID,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
