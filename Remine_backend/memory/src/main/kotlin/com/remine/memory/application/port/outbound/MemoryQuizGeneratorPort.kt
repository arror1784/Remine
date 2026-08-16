package com.remine.memory.application.port.outbound

import com.remine.memory.domain.MemoryPhoto

interface MemoryQuizGeneratorPort {
    /**
     * Step 1: Generates 3~4 question texts based on photo details (title, memoryLabel).
     * Returns pure question strings without options or answers.
     */
    fun generateDraftQuestions(photo: MemoryPhoto, count: Int = 3): List<String>

    /**
     * Step 3: Generates plausible distractors (wrong options, 3 per question) for each question and its real answer.
     */
    fun generateDistractors(items: List<QuestionAndAnswer>): List<GeneratedDistractors>

    data class QuestionAndAnswer(
        val question: String,
        val answer: String,
    )

    data class GeneratedDistractors(
        val question: String,
        val distractors: List<String>,
    )
}
