package com.remine.family.adapter.presentation.web

import com.remine.family.domain.PostWithViewerState
import java.time.Instant
import java.util.UUID

data class PostWithViewerStateResponse(
    val id: UUID,
    val authorUserId: UUID,
    val body: String,
    val photoUrl: String?,
    val photoCaption: String?,
    val likeCount: Int,
    val likedByViewer: Boolean,
    val replyCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(item: PostWithViewerState): PostWithViewerStateResponse = PostWithViewerStateResponse(
            id = item.post.id,
            authorUserId = item.post.authorUserId,
            body = item.post.body,
            photoUrl = item.post.photoUrl,
            photoCaption = item.post.photoCaption,
            likeCount = item.post.likeCount,
            likedByViewer = item.likedByViewer,
            replyCount = item.replyCount,
            createdAt = item.post.createdAt,
            updatedAt = item.post.updatedAt,
        )
    }
}
