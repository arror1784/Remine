package com.remine.family.application.port.inbound

import java.util.UUID

interface ToggleFamilyPostLikeCommand {
    fun handle(command: In): Out

    data class In(
        val postId: UUID,
        val userId: UUID,
        val pairUserIds: Set<UUID>,
    )

    data class Out(
        val liked: Boolean,
        val likeCount: Int,
    )
}
