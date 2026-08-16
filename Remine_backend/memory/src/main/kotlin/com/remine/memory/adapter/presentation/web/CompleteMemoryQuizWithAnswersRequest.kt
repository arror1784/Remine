package com.remine.memory.adapter.presentation.web

import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class CompleteMemoryQuizWithAnswersRequest(
    @field:NotEmpty(message = "답변 목록은 비어있을 수 없습니다")
    @field:Valid
    val answers: List<DraftAnswerRequest>,
)

data class DraftAnswerRequest(
    val questionId: UUID? = null,
    val question: String? = null,
    @field:NotBlank(message = "정답 내용은 필수입니다")
    val answer: String,
)
