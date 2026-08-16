package com.remine.user.adapter.presentation.web

import java.util.UUID

data class SignUpResponse(
    val userId: UUID,
    val inviteCode: String?,
    val accessToken: String,
)
