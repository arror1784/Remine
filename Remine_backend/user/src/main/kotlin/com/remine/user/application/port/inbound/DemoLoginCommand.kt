package com.remine.user.application.port.inbound

import com.remine.auth.domain.Role
import com.remine.user.domain.DemoVariant
import java.util.UUID

interface DemoLoginCommand {
    fun handle(command: In): Out

    data class In(
        val role: Role,
        val variant: DemoVariant = DemoVariant.EVAL,
    )

    data class Out(
        val userId: UUID,
        val role: Role,
        val name: String,
        val accessToken: String,
        val pairedUserId: UUID?,
    )
}
