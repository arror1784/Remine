package com.remine.family.adapter.presentation.web

import com.remine.family.application.port.inbound.ToggleFamilyPostLikeCommand

data class ToggleLikeResponse(
    val liked: Boolean,
    val likeCount: Int,
) {
    companion object {
        fun from(out: ToggleFamilyPostLikeCommand.Out): ToggleLikeResponse = ToggleLikeResponse(
            liked = out.liked,
            likeCount = out.likeCount,
        )
    }
}
