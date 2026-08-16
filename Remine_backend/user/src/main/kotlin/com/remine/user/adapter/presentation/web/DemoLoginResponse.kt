package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.Role
import java.util.UUID

data class DemoLoginResponse(
    val userId: UUID,
    val role: Role,
    val name: String,
    val accessToken: String,
    val pairedUserId: UUID?,
)
