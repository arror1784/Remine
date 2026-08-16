package com.remine.family.adapter.presentation.web

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class CreateFamilyPostReplyRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val body: String,
)
