package com.remine.memory.domain

import java.util.UUID

data class QuestionView(
    val id: UUID,
    val question: String,
    val options: List<String>,
)
