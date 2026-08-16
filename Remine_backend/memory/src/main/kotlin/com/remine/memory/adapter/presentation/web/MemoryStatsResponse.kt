package com.remine.memory.adapter.presentation.web

data class MemoryStatsResponse(
    val totalPhotos: Int,
    val quizActiveCount: Int,
    val addedThisMonth: Int,
)
