package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.GenerateMemoryQuizCommand
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FakeMemoryQuizGenerator(
    private val generated: List<MemoryQuizGeneratorPort.GeneratedQuestion>,
) : MemoryQuizGeneratorPort {
    var callCount = 0

    override fun generateQuestions(photo: MemoryPhoto, count: Int): List<MemoryQuizGeneratorPort.GeneratedQuestion> {
        callCount++
        return generated
    }
}

class GenerateMemoryQuizServiceTest {

    private lateinit var photoRepository: FakeMemoryPhotoRepository
    private lateinit var questionRepository: FakeMemoryQuizQuestionRepository

    private val photoId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()

    private val generated = listOf(
        MemoryQuizGeneratorPort.GeneratedQuestion(
            question = "이 사진은 언제쯤 찍은 걸까요?",
            options = listOf("2020년 봄", "2021년 봄", "2022년 봄", "2023년 봄"),
            correctOptionIndex = 2,
        ),
        MemoryQuizGeneratorPort.GeneratedQuestion(
            question = "이날 우리 가족은 무엇을 했을까요?",
            options = listOf("여행", "이사", "졸업식", "생신"),
            correctOptionIndex = 0,
        ),
    )

    @BeforeEach
    fun setUp() {
        photoRepository = FakeMemoryPhotoRepository()
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
        GenerateMemoryQuizService(photoRepository, questionRepository, generator)

    @Test
    fun `persists generated questions and sets status to QUIZ_ACTIVE`() {
        savePhoto()
        val generator = FakeMemoryQuizGenerator(generated)

        val result = service(generator).handle(
            GenerateMemoryQuizCommand.In(memoryPhotoId = photoId, ownerUserId = ownerId),
        )

        assertEquals(1, generator.callCount)
        assertEquals(2, result.questions.size)
        assertEquals(photoId, result.questions[0].memoryPhotoId)
        assertEquals("이 사진은 언제쯤 찍은 걸까요?", result.questions[0].question)
        assertEquals(2, result.questions[0].correctOptionIndex)
        assertEquals(listOf(0, 1), result.questions.map { it.sortOrder })
        assertEquals(2, questionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)
        assertEquals(MemoryPhotoStatus.QUIZ_ACTIVE, photoRepository.findById(photoId)?.status)
    }

    @Test
    fun `throws when photo not found`() {
        assertThrows<EntityNotFoundException> {
            service(FakeMemoryQuizGenerator(generated)).handle(
                GenerateMemoryQuizCommand.In(memoryPhotoId = photoId, ownerUserId = ownerId),
            )
        }
    }

    @Test
    fun `rejects a photo owned by another family without calling the generator`() {
        savePhoto()
        val generator = FakeMemoryQuizGenerator(generated)

        assertThrows<ForbiddenException> {
            service(generator).handle(
                GenerateMemoryQuizCommand.In(memoryPhotoId = photoId, ownerUserId = UUID.randomUUID()),
            )
        }

        assertEquals(0, generator.callCount)
        assertEquals(0, questionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)
        assertEquals(MemoryPhotoStatus.PENDING, photoRepository.findById(photoId)?.status)
    }

    @Test
    fun `throws and leaves the photo untouched when the generator returns nothing`() {
        savePhoto()

        assertThrows<InvalidRequestException> {
            service(FakeMemoryQuizGenerator(emptyList())).handle(
                GenerateMemoryQuizCommand.In(memoryPhotoId = photoId, ownerUserId = ownerId),
            )
        }

        assertEquals(0, questionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)
        assertEquals(MemoryPhotoStatus.PENDING, photoRepository.findById(photoId)?.status)
    }
}
