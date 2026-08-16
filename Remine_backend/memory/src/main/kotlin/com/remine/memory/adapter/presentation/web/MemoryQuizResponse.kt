package com.remine.memory.adapter.presentation.web

data class MemoryQuizResponse(
    val photo: MemoryPhotoResponse,
    val questions: List<QuestionViewResponse>,
)
