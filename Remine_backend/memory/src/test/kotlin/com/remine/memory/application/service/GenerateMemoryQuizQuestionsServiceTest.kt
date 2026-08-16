package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.GenerateMemoryQuizQuestionsCommand
import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizDraftQuestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FakeMemoryQuizDraftQuestionRepository : MemoryQuizDraftQuestionRepositoryPort {
    val draftQuestions = mutableListOf<MemoryQuizDraftQuestion>()

    override fun saveAll(questions: List<MemoryQuizDraftQuestion>): List<MemoryQuizDraftQuestion> {
        draftQuestions.addAll(questions)
        return questions
    }

    override fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizDraftQuestion> =
        draftQuestions.filter { it.memoryPhotoId == memoryPhotoId && it.deletedAt == null }.sortedBy { it.sortOrder }

    override fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID) {
        val toDelete = draftQuestions.filter { it.memoryPhotoId == memoryPhotoId && it.deletedAt == null }
        draftQuestions.removeAll(toDelete)
        draftQuestions.addAll(toDelete.map { it.copy(deletedAt = java.time.Instant.now()) })
    }
}

class FakeQuizGeneratorForDraft(
    private val draftQuestions: List<String>,
) : MemoryQuizGeneratorPort {
    var callCount = 0

    override fun generateDraftQuestions(photo: MemoryPhoto, count: Int): List<String> {
        callCount++
        return draftQuestions
    }

    override fun generateDistractors(items: List<MemoryQuizGeneratorPort.QuestionAndAnswer>): List<MemoryQuizGeneratorPort.GeneratedDistractors> =
        emptyList()
}

class GenerateMemoryQuizQuestionsServiceTest {

    private lateinit var photoRepository: FakeMemoryPhotoRepository
    private lateinit var draftQuestionRepository: FakeMemoryQuizDraftQuestionRepository

    private val photoId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()

    private val sampleDrafts = listOf(
        "이 사진은 언제쯤 찍은 걸까요?",
        "이날 우리 가족은 무엇을 했을까요?",
        "이 사진에서 가장 기억에 남는 것은 무엇인가요?",
    )

    @BeforeEach
    fun setUp() {
        photoRepository = FakeMemoryPhotoRepository()
        draftQuestionRepository = FakeMemoryQuizDraftQuestionRepository()
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
        GenerateMemoryQuizQuestionsService(photoRepository, draftQuestionRepository, generator)

    @Test
    fun `persists draft questions and replaces existing drafts on re-generation`() {
        savePhoto()
        val generator = FakeQuizGeneratorForDraft(sampleDrafts)

        // First call
        val result1 = service(generator).handle(
            GenerateMemoryQuizQuestionsCommand.In(memoryPhotoId = photoId, ownerUserId = ownerId),
        )

        assertEquals(1, generator.callCount)
        assertEquals(3, result1.questions.size)
        assertEquals(photoId, result1.questions[0].memoryPhotoId)
        assertEquals("이 사진은 언제쯤 찍은 걸까요?", result1.questions[0].question)
        assertEquals(listOf(0, 1, 2), result1.questions.map { it.sortOrder })
        assertEquals(3, draftQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)

        // Second call on the same photo (regenerating draft questions)
        val newDrafts = listOf("새로운 질문 1", "새로운 질문 2", "새로운 질문 3")
        val generator2 = FakeQuizGeneratorForDraft(newDrafts)
        service(generator2).handle(
            GenerateMemoryQuizQuestionsCommand.In(memoryPhotoId = photoId, ownerUserId = ownerId),
        )

        // Verifies no duplicate accumulation occurred
        val currentDrafts = draftQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId)
        assertEquals(3, currentDrafts.size)
        assertEquals(listOf("새로운 질문 1", "새로운 질문 2", "새로운 질문 3"), currentDrafts.map { it.question })
        assertEquals(listOf(0, 1, 2), currentDrafts.map { it.sortOrder })
    }

    @Test
    fun `throws when photo not found`() {
        assertThrows<EntityNotFoundException> {
            service(FakeQuizGeneratorForDraft(sampleDrafts)).handle(
                GenerateMemoryQuizQuestionsCommand.In(memoryPhotoId = photoId, ownerUserId = ownerId),
            )
        }
    }

    @Test
    fun `rejects a photo owned by another family without calling the generator`() {
        savePhoto()
        val generator = FakeQuizGeneratorForDraft(sampleDrafts)

        assertThrows<ForbiddenException> {
            service(generator).handle(
                GenerateMemoryQuizQuestionsCommand.In(memoryPhotoId = photoId, ownerUserId = UUID.randomUUID()),
            )
        }

        assertEquals(0, generator.callCount)
        assertEquals(0, draftQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)
    }

    @Test
    fun `throws and leaves draft repository untouched when generator returns nothing`() {
        savePhoto()

        assertThrows<InvalidRequestException> {
            service(FakeQuizGeneratorForDraft(emptyList())).handle(
                GenerateMemoryQuizQuestionsCommand.In(memoryPhotoId = photoId, ownerUserId = ownerId),
            )
        }

        assertEquals(0, draftQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)
    }
}
