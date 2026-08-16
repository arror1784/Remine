package com.remine.family.domain

data class PostWithViewerState(
    val post: FamilyPost,
    val likedByViewer: Boolean,
    val replyCount: Int,
)
