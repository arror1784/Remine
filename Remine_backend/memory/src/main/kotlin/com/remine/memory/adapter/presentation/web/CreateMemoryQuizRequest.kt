package com.remine.memory.adapter.presentation.web

import javax.validation.Valid
import javax.validation.constraints.NotEmpty

data class CreateMemoryQuizRequest(
    @field:NotEmpty
    @field:Valid
    val questions: List<CreateQuizQuestionRequest>,
)
