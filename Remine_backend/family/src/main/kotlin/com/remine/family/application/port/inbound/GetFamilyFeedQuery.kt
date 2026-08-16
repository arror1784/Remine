package com.remine.family.application.port.inbound

import com.remine.family.domain.PostWithViewerState
import java.time.Instant
import java.util.UUID

interface GetFamilyFeedQuery {
    fun handle(query: In): Out

    data class In(
        val pairUserIds: Set<UUID>,
        val viewerUserId: UUID,
        val cursor: Instant? = null,
        val limit: Int = 20,
    )

    data class Out(
        val items: List<PostWithViewerState>,
    )
}
