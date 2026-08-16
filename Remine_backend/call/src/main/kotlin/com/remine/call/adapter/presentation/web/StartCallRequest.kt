package com.remine.call.adapter.presentation.web

import java.util.UUID

data class StartCallRequest(
    val calleeId: UUID? = null,
)
