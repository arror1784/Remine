package com.remine.family.application.port.inbound

import com.remine.family.domain.FamilyPostReply
import java.util.UUID

interface GetFamilyPostRepliesQuery {
    fun handle(query: In): Out

    data class In(
        val postId: UUID,
        val pairUserIds: Set<UUID>,
    )

    data class Out(
        val items: List<FamilyPostReply>,
    )
}
