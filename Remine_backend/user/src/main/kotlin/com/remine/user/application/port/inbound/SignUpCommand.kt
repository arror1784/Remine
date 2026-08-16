package com.remine.user.application.port.inbound

import com.remine.auth.domain.Role
import java.util.UUID

interface SignUpCommand {
    fun handle(command: In): Out

    data class In(
        val role: Role,
        val name: String,
        val ageGroup: String,
        val interests: List<String> = emptyList(),
        val email: String? = null,
        val googleId: String? = null,
    )

    data class Out(
        val userId: UUID,
        val inviteCode: String?,
        val accessToken: String,
    )
}
