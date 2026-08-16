package com.remine.memory.adapter.presentation.web

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class CreateQuizQuestionRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val question: String,

    @field:NotEmpty
    val options: List<String>,

    @field:Min(0)
    val correctOptionIndex: Int,
)
