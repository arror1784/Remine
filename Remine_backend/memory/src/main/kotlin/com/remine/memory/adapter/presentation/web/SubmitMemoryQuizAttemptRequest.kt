package com.remine.memory.adapter.presentation.web

import javax.validation.constraints.NotNull

data class SubmitMemoryQuizAttemptRequest(
    @field:NotNull
    val answers: List<Int>,
)
