package com.remine.family.application.port.inbound

import com.remine.family.domain.FamilyPostReply
import java.util.UUID

interface CreateFamilyPostReplyCommand {
    fun handle(command: In): Out

    data class In(
        val postId: UUID,
        val authorUserId: UUID,
        val body: String,
        val pairUserIds: Set<UUID>,
    )

    data class Out(
        val entity: FamilyPostReply,
    )
}
