package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.CompleteMemoryQuizWithAnswersCommand
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizDraftQuestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FakeQuizGeneratorForDistractors(
    private val distractorMap: Map<String, List<String>>,
) : MemoryQuizGeneratorPort {
    var callCount = 0

    override fun generateDraftQuestions(photo: MemoryPhoto, count: Int): List<String> = emptyList()

    override fun generateDistractors(
        items: List<MemoryQuizGeneratorPort.QuestionAndAnswer>,
    ): List<MemoryQuizGeneratorPort.GeneratedDistractors> {
        callCount++
        return items.map { item ->
            MemoryQuizGeneratorPort.GeneratedDistractors(
                question = item.question,
                distractors = distractorMap[item.question] ?: listOf("오답 1", "오답 2", "오답 3"),
            )
        }
    }
}

class CompleteMemoryQuizWithAnswersServiceTest {

    private lateinit var photoRepository: FakeMemoryPhotoRepository
    private lateinit var draftQuestionRepository: FakeMemoryQuizDraftQuestionRepository
    private lateinit var questionRepository: FakeMemoryQuizQuestionRepository

    private val photoId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        photoRepository = FakeMemoryPhotoRepository()
        draftQuestionRepository = FakeMemoryQuizDraftQuestionRepository()
        questionRepository = FakeMemoryQuizQuestionRepository()
    }

    private fun savePhoto() = photoRepository.save(
        MemoryPhoto(
            id = photoId,
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "가족 여행",
            photoUrl = "https://example.com/photo.jpg",
            memoryLabel = "2022년 봄",
            status = MemoryPhotoStatus.PENDING,
        ),
    )

    private fun service(generator: MemoryQuizGeneratorPort) =
        CompleteMemoryQuizWithAnswersService(
            photoRepository,
            draftQuestionRepository,
            questionRepository,
            generator,
        )

    @Test
    fun `generates distractors, saves 4-choice quiz with correct answer index, and sets status to QUIZ_ACTIVE`() {
        savePhoto()
        val draft1 = MemoryQuizDraftQuestion(memoryPhotoId = photoId, question = "이 사진은 언제쯤 찍었을까요?", sortOrder = 0)
        val draft2 = MemoryQuizDraftQuestion(memoryPhotoId = photoId, question = "이날 어디에 갔을까요?", sortOrder = 1)
        draftQuestionRepository.saveAll(listOf(draft1, draft2))

        val distractorMap = mapOf(
            "이 사진은 언제쯤 찍었을까요?" to listOf("2020년 봄", "2021년 봄", "2023년 봄"),
            "이날 어디에 갔을까요?" to listOf("부산 해운대", "강릉 경포대", "경주 불국사"),
        )
        val generator = FakeQuizGeneratorForDistractors(distractorMap)

        val result = service(generator).handle(
            CompleteMemoryQuizWithAnswersCommand.In(
                memoryPhotoId = photoId,
                ownerUserId = ownerId,
                answers = listOf(
                    CompleteMemoryQuizWithAnswersCommand.AnswerIn(
                        draftQuestionId = draft1.id,
                        answer = "2022년 봄",
                    ),
                    CompleteMemoryQuizWithAnswersCommand.AnswerIn(
                        draftQuestionId = draft2.id,
                        answer = "제주도 성산일출봉",
                    ),
                ),
            ),
        )

        assertEquals(1, generator.callCount)
        assertEquals(2, result.questions.size)

        val q1 = result.questions[0]
        assertEquals("이 사진은 언제쯤 찍었을까요?", q1.question)
        assertEquals(4, q1.options.size)
        assertTrue(q1.options.contains("2022년 봄"))
        assertEquals("2022년 봄", q1.options[q1.correctOptionIndex])
        assertEquals(0, q1.sortOrder)

        val q2 = result.questions[1]
        assertEquals("이날 어디에 갔을까요?", q2.question)
        assertEquals(4, q2.options.size)
        assertTrue(q2.options.contains("제주도 성산일출봉"))
        assertEquals("제주도 성산일출봉", q2.options[q2.correctOptionIndex])
        assertEquals(1, q2.sortOrder)

        // Photo status updated to QUIZ_ACTIVE
        assertEquals(MemoryPhotoStatus.QUIZ_ACTIVE, photoRepository.findById(photoId)?.status)

        // Draft questions cleared
        assertEquals(0, draftQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)

        // Final questions saved
        assertEquals(2, questionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)
    }

    @Test
    fun `re-completing replaces previous final questions cleanly without duplicate accumulation`() {
        savePhoto()
        val draft = MemoryQuizDraftQuestion(memoryPhotoId = photoId, question = "질문", sortOrder = 0)
        draftQuestionRepository.saveAll(listOf(draft))

        val generator = FakeQuizGeneratorForDistractors(mapOf("질문" to listOf("오답1", "오답2", "오답3")))

        // 1st completion
        service(generator).handle(
            CompleteMemoryQuizWithAnswersCommand.In(
                memoryPhotoId = photoId,
                ownerUserId = ownerId,
                answers = listOf(
                    CompleteMemoryQuizWithAnswersCommand.AnswerIn(
                        question = "질문",
                        answer = "정답1",
                    ),
                ),
            ),
        )
        assertEquals(1, questionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)

        // 2nd completion on same photo
        service(generator).handle(
            CompleteMemoryQuizWithAnswersCommand.In(
                memoryPhotoId = photoId,
                ownerUserId = ownerId,
                answers = listOf(
                    CompleteMemoryQuizWithAnswersCommand.AnswerIn(
                        question = "새 질문",
                        answer = "새 정답",
                    ),
                ),
            ),
        )

        // Verifies previous final questions were deleted and replaced cleanly (not accumulated to 2)
        val finalQuestions = questionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId)
        assertEquals(1, finalQuestions.size)
        assertEquals("새 질문", finalQuestions[0].question)
        assertEquals(0, finalQuestions[0].sortOrder)
    }

    @Test
    fun `throws when photo not found`() {
        assertThrows<EntityNotFoundException> {
            service(FakeQuizGeneratorForDistractors(emptyMap())).handle(
                CompleteMemoryQuizWithAnswersCommand.In(
                    memoryPhotoId = photoId,
                    ownerUserId = ownerId,
                    answers = listOf(
                        CompleteMemoryQuizWithAnswersCommand.AnswerIn(question = "Q", answer = "A"),
                    ),
                ),
            )
        }
    }

    @Test
    fun `rejects a photo owned by another family`() {
        savePhoto()

        assertThrows<ForbiddenException> {
            service(FakeQuizGeneratorForDistractors(emptyMap())).handle(
                CompleteMemoryQuizWithAnswersCommand.In(
                    memoryPhotoId = photoId,
                    ownerUserId = UUID.randomUUID(),
                    answers = listOf(
                        CompleteMemoryQuizWithAnswersCommand.AnswerIn(question = "Q", answer = "A"),
                    ),
                ),
            )
        }
    }

    @Test
    fun `throws when answers list is empty or answer is blank`() {
        savePhoto()
        val service = service(FakeQuizGeneratorForDistractors(emptyMap()))

        assertThrows<InvalidRequestException> {
            service.handle(
                CompleteMemoryQuizWithAnswersCommand.In(
                    memoryPhotoId = photoId,
                    ownerUserId = ownerId,
                    answers = emptyList(),
                ),
            )
        }

        assertThrows<InvalidRequestException> {
            service.handle(
                CompleteMemoryQuizWithAnswersCommand.In(
                    memoryPhotoId = photoId,
                    ownerUserId = ownerId,
                    answers = listOf(
                        CompleteMemoryQuizWithAnswersCommand.AnswerIn(question = "Q", answer = "   "),
                    ),
                ),
            )
        }
    }

    @Test
    fun `throws when distractors returned by AI are insufficient`() {
        savePhoto()
        val generator = object : MemoryQuizGeneratorPort {
            override fun generateDraftQuestions(photo: MemoryPhoto, count: Int): List<String> = emptyList()
            override fun generateDistractors(items: List<MemoryQuizGeneratorPort.QuestionAndAnswer>) =
                listOf(MemoryQuizGeneratorPort.GeneratedDistractors(question = "Q", distractors = listOf("오답1")))
        }

        assertThrows<InvalidRequestException> {
            service(generator).handle(
                CompleteMemoryQuizWithAnswersCommand.In(
                    memoryPhotoId = photoId,
                    ownerUserId = ownerId,
                    answers = listOf(
                        CompleteMemoryQuizWithAnswersCommand.AnswerIn(question = "Q", answer = "정답"),
                    ),
                ),
            )
        }
    }
}
