package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.Role
import javax.validation.constraints.NotNull

data class DemoLoginRequest(
    @field:NotNull
    val role: Role,
)
