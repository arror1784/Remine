package com.remine.family.adapter.presentation.web

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class CreateFamilyPostRequest(
    @field:NotBlank
    @field:Size(max = 1000)
    val body: String,

    @field:Size(max = 500)
    val photoUrl: String? = null,

    @field:Size(max = 200)
    val photoCaption: String? = null,
)
