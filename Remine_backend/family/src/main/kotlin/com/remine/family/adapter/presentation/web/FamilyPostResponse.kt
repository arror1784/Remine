package com.remine.family.adapter.presentation.web

import com.remine.family.domain.FamilyPost
import java.time.Instant
import java.util.UUID

data class FamilyPostResponse(
    val id: UUID,
    val authorUserId: UUID,
    val body: String,
    val photoUrl: String?,
    val photoCaption: String?,
    val likeCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: FamilyPost): FamilyPostResponse = FamilyPostResponse(
            id = entity.id,
            authorUserId = entity.authorUserId,
            body = entity.body,
            photoUrl = entity.photoUrl,
            photoCaption = entity.photoCaption,
            likeCount = entity.likeCount,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
