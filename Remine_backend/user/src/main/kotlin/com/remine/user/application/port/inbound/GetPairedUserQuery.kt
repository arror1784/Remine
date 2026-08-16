package com.remine.user.application.port.inbound

import com.remine.user.domain.User
import java.util.UUID

interface GetPairedUserQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
    )

    data class Out(
        val pairedEntity: User?,
    )
}
