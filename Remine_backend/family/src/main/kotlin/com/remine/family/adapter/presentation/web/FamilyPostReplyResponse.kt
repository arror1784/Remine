package com.remine.family.adapter.presentation.web

import com.remine.family.domain.FamilyPostReply
import java.time.Instant
import java.util.UUID

data class FamilyPostReplyResponse(
    val id: UUID,
    val postId: UUID,
    val authorUserId: UUID,
    val body: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(entity: FamilyPostReply): FamilyPostReplyResponse = FamilyPostReplyResponse(
            id = entity.id,
            postId = entity.postId,
            authorUserId = entity.authorUserId,
            body = entity.body,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }
}
