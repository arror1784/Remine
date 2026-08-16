package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.Role
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class SignUpRequest(
    @field:NotNull(message = "Role is required")
    val role: Role,

    @field:NotBlank(message = "Name is required")
    @field:Size(max = 50)
    val name: String,

    @field:NotBlank(message = "Age group is required")
    @field:Size(max = 10)
    val ageGroup: String,

    // Persisted as a single comma-joined varchar(255) column on app_user.
    @field:Size(max = 10)
    val interests: List<@Size(max = 20) String> = emptyList(),

    @field:Size(max = 255)
    val email: String? = null,

    @field:Size(max = 255)
    val googleId: String? = null,
)
