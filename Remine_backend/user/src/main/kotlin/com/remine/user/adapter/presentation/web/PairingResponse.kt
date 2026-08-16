package com.remine.user.adapter.presentation.web

import java.util.UUID

data class PairingResponse(
    val parentUserId: UUID,
    val accessToken: String,
)
