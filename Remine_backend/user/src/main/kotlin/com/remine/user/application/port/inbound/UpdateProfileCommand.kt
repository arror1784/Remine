package com.remine.user.application.port.inbound

import com.remine.user.domain.User
import java.util.UUID

interface UpdateProfileCommand {
    fun handle(command: In): Out

    data class In(
        val userId: UUID,
        val name: String?,
        val ageGroup: String?,
        val interests: List<String>?,
    )

    data class Out(
        val entity: User,
    )
}
