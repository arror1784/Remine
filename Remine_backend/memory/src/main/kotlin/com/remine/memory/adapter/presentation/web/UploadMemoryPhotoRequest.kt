package com.remine.memory.adapter.presentation.web

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class UploadMemoryPhotoRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val title: String,

    @field:NotBlank
    @field:Size(max = 500)
    val photoUrl: String,

    @field:NotBlank
    @field:Size(max = 50)
    val memoryLabel: String,
)
