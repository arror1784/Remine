package com.remine.memory.adapter.presentation.web

data class TodayQuizResponse(
    val photo: MemoryPhotoResponse?,
    val questions: List<QuestionViewResponse>,
)
