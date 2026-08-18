package com.remine.memory.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.common.web.ApiResponse
import com.remine.memory.application.port.inbound.CompleteMemoryQuizWithAnswersCommand
import com.remine.memory.application.port.inbound.CreateMemoryQuizCommand
import com.remine.memory.application.port.inbound.GenerateMemoryQuizQuestionsCommand
import com.remine.memory.application.port.inbound.GetMemoryGalleryQuery
import com.remine.memory.application.port.inbound.GetMemoryQuizDraftQuestionsQuery
import com.remine.memory.application.port.inbound.GetMemoryQuizQuery
import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.memory.application.port.inbound.GetTodayQuizQuery
import com.remine.memory.application.port.inbound.SubmitMemoryQuizAttemptCommand
import com.remine.memory.application.port.inbound.UploadMemoryPhotoCommand
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/memories")
class MemoryController(
    private val uploadMemoryPhotoCommand: UploadMemoryPhotoCommand,
    private val getMemoryGalleryQuery: GetMemoryGalleryQuery,
    private val getMemoryStatsQuery: GetMemoryStatsQuery,
    private val createMemoryQuizCommand: CreateMemoryQuizCommand,
    private val generateMemoryQuizQuestionsCommand: GenerateMemoryQuizQuestionsCommand,
    private val completeMemoryQuizWithAnswersCommand: CompleteMemoryQuizWithAnswersCommand,
    private val getMemoryQuizDraftQuestionsQuery: GetMemoryQuizDraftQuestionsQuery,
    private val getMemoryQuizQuery: GetMemoryQuizQuery,
    private val getTodayQuizQuery: GetTodayQuizQuery,
    private val submitMemoryQuizAttemptCommand: SubmitMemoryQuizAttemptCommand,
) {

    @PostMapping
    fun uploadMemoryPhoto(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: UploadMemoryPhotoRequest,
    ): ApiResponse<MemoryPhotoResponse> {
        val result = uploadMemoryPhotoCommand.handle(
            UploadMemoryPhotoCommand.In(
                uploadedByUserId = principal.userId,
                ownerUserId = principal.parentUserId(),
                title = request.title,
                photoUrl = request.photoUrl,
                memoryLabel = request.memoryLabel,
            ),
        )
        return ApiResponse.ok(MemoryPhotoResponse.from(result.entity))
    }

    @GetMapping
    fun getMemoryGallery(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<List<MemoryPhotoResponse>> {
        val result = getMemoryGalleryQuery.handle(
            GetMemoryGalleryQuery.In(
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(result.items.map { MemoryPhotoResponse.from(it, attempted = result.attemptedPhotoIds.contains(it.id)) })
    }

    @GetMapping("/stats")
    fun getMemoryStats(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<MemoryStatsResponse> {
        val result = getMemoryStatsQuery.handle(
            GetMemoryStatsQuery.In(
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(
            MemoryStatsResponse(
                totalPhotos = result.totalPhotos,
                quizActiveCount = result.quizActiveCount,
                addedThisMonth = result.addedThisMonth,
            ),
        )
    }

    // Manual quiz creation (kept unchanged for custom family quizzes)
    @PostMapping("/{id}/quiz")
    fun createMemoryQuiz(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateMemoryQuizRequest,
    ): ApiResponse<List<MemoryQuizQuestionResponse>> {
        val result = createMemoryQuizCommand.handle(
            CreateMemoryQuizCommand.In(
                memoryPhotoId = id,
                questions = request.questions.map {
                    CreateMemoryQuizCommand.QuestionIn(
                        question = it.question,
                        options = it.options,
                        correctOptionIndex = it.correctOptionIndex,
                    )
                },
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(result.questions.map { MemoryQuizQuestionResponse.from(it) })
    }

    /**
     * Step 1: AI generates pure questions without options/answers for the child.
     */
    @PostMapping("/{id}/quiz/generate-questions")
    fun generateMemoryQuizQuestions(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
    ): ApiResponse<List<MemoryQuizDraftQuestionResponse>> {
        val result = generateMemoryQuizQuestionsCommand.handle(
            GenerateMemoryQuizQuestionsCommand.In(
                memoryPhotoId = id,
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(result.questions.map { MemoryQuizDraftQuestionResponse.from(it) })
    }

    /**
     * Step 3: Given child's real answers, AI generates distractors and saves final 4-choice quiz.
     */
    @PostMapping("/{id}/quiz/complete-with-answers")
    fun completeMemoryQuizWithAnswers(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CompleteMemoryQuizWithAnswersRequest,
    ): ApiResponse<List<MemoryQuizQuestionResponse>> {
        val result = completeMemoryQuizWithAnswersCommand.handle(
            CompleteMemoryQuizWithAnswersCommand.In(
                memoryPhotoId = id,
                ownerUserId = principal.parentUserId(),
                answers = request.answers.map {
                    CompleteMemoryQuizWithAnswersCommand.AnswerIn(
                        draftQuestionId = it.questionId,
                        question = it.question,
                        answer = it.answer,
                    )
                },
            ),
        )
        return ApiResponse.ok(result.questions.map { MemoryQuizQuestionResponse.from(it) })
    }

    /**
     * Query draft questions for a memory photo.
     */
    @GetMapping("/{id}/quiz/draft-questions")
    fun getMemoryQuizDraftQuestions(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
    ): ApiResponse<List<MemoryQuizDraftQuestionResponse>> {
        val result = getMemoryQuizDraftQuestionsQuery.handle(
            GetMemoryQuizDraftQuestionsQuery.In(
                memoryPhotoId = id,
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(result.questions.map { MemoryQuizDraftQuestionResponse.from(it) })
    }

    @GetMapping("/{id}/quiz")
    fun getMemoryQuiz(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
    ): ApiResponse<MemoryQuizResponse> {
        val result = getMemoryQuizQuery.handle(
            GetMemoryQuizQuery.In(
                memoryPhotoId = id,
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(
            MemoryQuizResponse(
                photo = MemoryPhotoResponse.from(result.photo),
                questions = result.questions.map { QuestionViewResponse.from(it) },
            ),
        )
    }

    @GetMapping("/quiz/today")
    fun getTodayQuiz(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<TodayQuizResponse> {
        val result = getTodayQuizQuery.handle(
            GetTodayQuizQuery.In(
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(
            TodayQuizResponse(
                photo = result.photo?.let { MemoryPhotoResponse.from(it) },
                questions = result.questions.map { QuestionViewResponse.from(it) },
            ),
        )
    }

    @PostMapping("/{id}/quiz/attempts")
    fun submitQuizAttempt(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: SubmitMemoryQuizAttemptRequest,
    ): ApiResponse<SubmitMemoryQuizAttemptResponse> {
        val result = submitMemoryQuizAttemptCommand.handle(
            SubmitMemoryQuizAttemptCommand.In(
                memoryPhotoId = id,
                respondentUserId = principal.userId,
                answers = request.answers,
                ownerUserId = principal.parentUserId(),
            ),
        )
        return ApiResponse.ok(
            SubmitMemoryQuizAttemptResponse(
                correctCount = result.correctCount,
                totalCount = result.totalCount,
            ),
        )
    }
}
