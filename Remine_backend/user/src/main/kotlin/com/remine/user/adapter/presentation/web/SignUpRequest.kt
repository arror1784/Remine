package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.Role
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class SignUpRequest(
    @field:NotNull(message = "Role is required")
    val role: Role,

    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Age group is required")
    val ageGroup: String,

    val interests: List<String> = emptyList(),
    val email: String? = null,
    val googleId: String? = null,
)
