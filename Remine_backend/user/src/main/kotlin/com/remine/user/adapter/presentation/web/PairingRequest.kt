package com.remine.user.adapter.presentation.web

import javax.validation.constraints.NotBlank

data class PairingRequest(
    @field:NotBlank(message = "Invite code is required")
    val inviteCode: String,
)
