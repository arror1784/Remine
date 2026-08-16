package com.remine.family.domain

import java.time.Instant
import java.util.UUID

data class FamilyPost(
    val id: UUID = UUID.randomUUID(),
    val authorUserId: UUID,
    val body: String,
    val photoUrl: String? = null,
    val photoCaption: String? = null,
    val likeCount: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
