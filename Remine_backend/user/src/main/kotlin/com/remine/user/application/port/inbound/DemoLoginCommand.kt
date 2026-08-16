package com.remine.user.application.port.inbound

import com.remine.auth.domain.Role
import java.util.UUID

interface DemoLoginCommand {
    fun handle(command: In): Out

    data class In(
        val role: Role,
    )

    data class Out(
        val userId: UUID,
        val role: Role,
        val accessToken: String,
        val pairedUserId: UUID?,
    )
}
