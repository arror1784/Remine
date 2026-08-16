package com.remine.family.application.port.inbound

import com.remine.family.domain.FamilyPost
import java.util.UUID

interface CreateFamilyPostCommand {
    fun handle(command: In): Out

    data class In(
        val authorUserId: UUID,
        val body: String,
        val photoUrl: String? = null,
        val photoCaption: String? = null,
    )

    data class Out(
        val entity: FamilyPost,
    )
}
