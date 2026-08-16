package com.remine.memory.adapter.presentation.web

import com.remine.memory.domain.QuestionView
import java.util.UUID

data class QuestionViewResponse(
    val id: UUID,
    val question: String,
    val options: List<String>,
) {
    companion object {
        fun from(domain: QuestionView): QuestionViewResponse =
            QuestionViewResponse(
                id = domain.id,
                question = domain.question,
                options = domain.options,
            )
    }
}
