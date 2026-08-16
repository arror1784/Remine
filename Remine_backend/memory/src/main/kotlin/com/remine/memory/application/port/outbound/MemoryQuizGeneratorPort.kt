package com.remine.memory.application.port.outbound

import com.remine.memory.domain.MemoryPhoto

interface MemoryQuizGeneratorPort {
    fun generateQuestions(photo: MemoryPhoto, count: Int = 3): List<GeneratedQuestion>

    data class GeneratedQuestion(
        val question: String,
        val options: List<String>,
        val correctOptionIndex: Int,
    )
}
