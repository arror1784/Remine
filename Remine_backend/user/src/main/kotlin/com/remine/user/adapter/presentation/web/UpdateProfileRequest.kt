package com.remine.user.adapter.presentation.web

data class UpdateProfileRequest(
    val name: String? = null,
    val ageGroup: String? = null,
    val interests: List<String>? = null,
)
