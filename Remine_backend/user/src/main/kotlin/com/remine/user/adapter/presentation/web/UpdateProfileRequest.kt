package com.remine.user.adapter.presentation.web

import javax.validation.constraints.Size

data class UpdateProfileRequest(
    @field:Size(max = 50)
    val name: String? = null,

    @field:Size(max = 10)
    val ageGroup: String? = null,

    // Persisted as a single comma-joined varchar(255) column on app_user.
    @field:Size(max = 10)
    val interests: List<@Size(max = 20) String>? = null,
)
