package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.CreateMemoryQuizCommand
import com.remine.memory.application.port.inbound.GetMemoryGalleryQuery
import com.remine.memory.application.port.inbound.GetMemoryQuizQuery
import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.memory.application.port.inbound.GetTodayQuizQuery
import com.remine.memory.application.port.inbound.SubmitMemoryQuizAttemptCommand
import com.remine.memory.application.port.inbound.UploadMemoryPhotoCommand
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizAttemptRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizAttempt
import com.remine.memory.domain.MemoryQuizQuestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class FakeMemoryPhotoRepository : MemoryPhotoRepositoryPort {
    val photos = mutableMapOf<UUID, MemoryPhoto>()

    override fun save(photo: MemoryPhoto): MemoryPhoto {
        photos[photo.id] = photo
        return photo
    }

    override fun findById(id: UUID): MemoryPhoto? = photos[id]

    override fun findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId: UUID): List<MemoryPhoto> =
        photos.values.filter { it.ownerUserId == ownerUserId }.sortedByDescending { it.createdAt }

    override fun countByOwnerUserId(ownerUserId: UUID): Int =
        photos.values.count { it.ownerUserId == ownerUserId }

    override fun countByOwnerUserIdAndStatus(ownerUserId: UUID, status: MemoryPhotoStatus): Int =
        photos.values.count { it.ownerUserId == ownerUserId && it.status == status }

    override fun countByOwnerUserIdAndCreatedAtGreaterThanEqual(ownerUserId: UUID, startOfMonth: Instant): Int =
        photos.values.count { it.ownerUserId == ownerUserId && !it.createdAt.isBefore(startOfMonth) }

    override fun findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId: UUID, status: MemoryPhotoStatus): List<MemoryPhoto> =
        photos.values.filter { it.ownerUserId == ownerUserId && it.status == status }.sortedByDescending { it.createdAt }
}

class FakeMemoryQuizQuestionRepository : MemoryQuizQuestionRepositoryPort {
    val questions = mutableListOf<MemoryQuizQuestion>()

    override fun saveAll(questions: List<MemoryQuizQuestion>): List<MemoryQuizQuestion> {
        this.questions.addAll(questions)
        return questions
    }

    override fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizQuestion> =
        questions.filter { it.memoryPhotoId == memoryPhotoId && it.deletedAt == null }.sortedBy { it.sortOrder }

    override fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID) {
        val toDelete = questions.filter { it.memoryPhotoId == memoryPhotoId && it.deletedAt == null }
        questions.removeAll(toDelete)
        questions.addAll(toDelete.map { it.copy(deletedAt = java.time.Instant.now()) })
    }
}

class FakeMemoryQuizAttemptRepository : MemoryQuizAttemptRepositoryPort {
    val attempts = mutableListOf<MemoryQuizAttempt>()

    override fun save(attempt: MemoryQuizAttempt): MemoryQuizAttempt {
        attempts.add(attempt)
        return attempt
    }

    override fun existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId: UUID, since: Instant): Boolean =
        attempts.any { it.memoryPhotoId == memoryPhotoId && !it.completedAt.isBefore(since) }

    override fun findAttemptedPhotoIds(memoryPhotoIds: Collection<UUID>): Set<UUID> =
        attempts.filter { it.memoryPhotoId in memoryPhotoIds }.map { it.memoryPhotoId }.toSet()
}

class MemoryServicesTest {

    private lateinit var photoRepository: FakeMemoryPhotoRepository
    private lateinit var questionRepository: FakeMemoryQuizQuestionRepository
    private lateinit var attemptRepository: FakeMemoryQuizAttemptRepository

    @BeforeEach
    fun setUp() {
        photoRepository = FakeMemoryPhotoRepository()
        questionRepository = FakeMemoryQuizQuestionRepository()
        attemptRepository = FakeMemoryQuizAttemptRepository()
    }

    @Test
    fun `UploadMemoryPhotoService creates photo with PENDING status`() {
        val service = UploadMemoryPhotoService(photoRepository)
        val uploadedBy = UUID.randomUUID()
        val owner = UUID.randomUUID()

        val result = service.handle(
            UploadMemoryPhotoCommand.In(
                uploadedByUserId = uploadedBy,
                ownerUserId = owner,
                title = "Family trip",
                photoUrl = "https://example.com/photo.jpg",
                memoryLabel = "2022년 여름",
            ),
        )

        assertEquals(uploadedBy, result.entity.uploadedByUserId)
        assertEquals(owner, result.entity.ownerUserId)
        assertEquals("Family trip", result.entity.title)
        assertEquals("https://example.com/photo.jpg", result.entity.photoUrl)
        assertEquals("2022년 여름", result.entity.memoryLabel)
        assertEquals(MemoryPhotoStatus.PENDING, result.entity.status)
        assertNotNull(photoRepository.findById(result.entity.id))
    }

    @Test
    fun `GetMemoryGalleryService flags only photos with a saved attempt`() {
        val service = GetMemoryGalleryService(photoRepository, attemptRepository)
        val owner = UUID.randomUUID()
        val attemptedPhoto = MemoryPhoto(
            ownerUserId = owner,
            uploadedByUserId = UUID.randomUUID(),
            title = "Attempted",
            photoUrl = "url",
            memoryLabel = "label",
        )
        val unattemptedPhoto = MemoryPhoto(
            ownerUserId = owner,
            uploadedByUserId = UUID.randomUUID(),
            title = "Not attempted",
            photoUrl = "url",
            memoryLabel = "label",
        )
        photoRepository.save(attemptedPhoto)
        photoRepository.save(unattemptedPhoto)
        attemptRepository.save(
            MemoryQuizAttempt(
                memoryPhotoId = attemptedPhoto.id,
                respondentUserId = owner,
                correctCount = 1,
                totalCount = 1,
            ),
        )

        val result = service.handle(GetMemoryGalleryQuery.In(ownerUserId = owner))

        assertEquals(setOf(attemptedPhoto.id), result.attemptedPhotoIds)
    }

    @Test
    fun `CreateMemoryQuizService saves questions and sets status to QUIZ_ACTIVE`() {
        val service = CreateMemoryQuizService(photoRepository, questionRepository)
        val photoId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val photo = MemoryPhoto(
            id = photoId,
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Test",
            photoUrl = "url",
            memoryLabel = "label",
            status = MemoryPhotoStatus.PENDING,
        )
        photoRepository.save(photo)

        val result = service.handle(
            CreateMemoryQuizCommand.In(
                memoryPhotoId = photoId,
                questions = listOf(
                    CreateMemoryQuizCommand.QuestionIn(
                        question = "Where was this taken?",
                        options = listOf("Seoul", "Busan", "Jeju"),
                        correctOptionIndex = 2,
                    ),
                ),
                ownerUserId = ownerId,
            ),
        )

        assertEquals(1, result.questions.size)
        assertEquals(photoId, result.questions[0].memoryPhotoId)
        assertEquals("Where was this taken?", result.questions[0].question)
        assertEquals(0, result.questions[0].sortOrder)

        val updatedPhoto = photoRepository.findById(photoId)
        assertEquals(MemoryPhotoStatus.QUIZ_ACTIVE, updatedPhoto?.status)
    }

    @Test
    fun `CreateMemoryQuizService throws when photo not found`() {
        val service = CreateMemoryQuizService(photoRepository, questionRepository)
        val photoId = UUID.randomUUID()

        assertThrows<EntityNotFoundException> {
            service.handle(
                CreateMemoryQuizCommand.In(
                    memoryPhotoId = photoId,
                    questions = listOf(
                        CreateMemoryQuizCommand.QuestionIn(
                            question = "Q",
                            options = listOf("A", "B"),
                            correctOptionIndex = 0,
                        ),
                    ),
                    ownerUserId = UUID.randomUUID(),
                ),
            )
        }
    }

    @Test
    fun `CreateMemoryQuizService throws when questions list is empty`() {
        val service = CreateMemoryQuizService(photoRepository, questionRepository)
        val photoId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val photo = MemoryPhoto(
            id = photoId,
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Test",
            photoUrl = "url",
            memoryLabel = "label",
        )
        photoRepository.save(photo)

        assertThrows<InvalidRequestException> {
            service.handle(
                CreateMemoryQuizCommand.In(
                    memoryPhotoId = photoId,
                    questions = emptyList(),
                    ownerUserId = ownerId,
                ),
            )
        }
    }

    @Test
    fun `SubmitMemoryQuizAttemptService grades answers and saves attempt`() {
        val service = SubmitMemoryQuizAttemptService(photoRepository, questionRepository, attemptRepository)
        val photoId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val photo = MemoryPhoto(
            id = photoId,
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Test",
            photoUrl = "url",
            memoryLabel = "label",
        )
        photoRepository.save(photo)

        val questions = listOf(
            MemoryQuizQuestion(
                id = UUID.randomUUID(),
                memoryPhotoId = photoId,
                question = "Q1",
                options = listOf("A", "B"),
                correctOptionIndex = 0,
                sortOrder = 0,
            ),
            MemoryQuizQuestion(
                id = UUID.randomUUID(),
                memoryPhotoId = photoId,
                question = "Q2",
                options = listOf("A", "B", "C"),
                correctOptionIndex = 2,
                sortOrder = 1,
            ),
        )
        questionRepository.saveAll(questions)

        val result = service.handle(
            SubmitMemoryQuizAttemptCommand.In(
                memoryPhotoId = photoId,
                respondentUserId = userId,
                answers = listOf(0, 1), // First correct, second wrong
                ownerUserId = ownerId,
            ),
        )

        assertEquals(1, result.correctCount)
        assertEquals(2, result.totalCount)
        assertEquals(1, attemptRepository.attempts.size)
        assertEquals(photoId, attemptRepository.attempts[0].memoryPhotoId)
        assertEquals(userId, attemptRepository.attempts[0].respondentUserId)
    }

    @Test
    fun `GetMemoryStatsService returns correct aggregate counts`() {
        val service = GetMemoryStatsService(photoRepository)
        val ownerId = UUID.randomUUID()

        val photo1 = MemoryPhoto(
            id = UUID.randomUUID(),
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Photo 1",
            photoUrl = "url1",
            memoryLabel = "label1",
            status = MemoryPhotoStatus.QUIZ_ACTIVE,
            createdAt = Instant.now(),
        )
        val photo2 = MemoryPhoto(
            id = UUID.randomUUID(),
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Photo 2",
            photoUrl = "url2",
            memoryLabel = "label2",
            status = MemoryPhotoStatus.PENDING,
            createdAt = Instant.now(),
        )
        photoRepository.save(photo1)
        photoRepository.save(photo2)

        val result = service.handle(GetMemoryStatsQuery.In(ownerUserId = ownerId))

        assertEquals(2, result.totalPhotos)
        assertEquals(1, result.quizActiveCount)
        assertEquals(2, result.addedThisMonth)
    }

    @Test
    fun `GetMemoryQuizService does not leak correctOptionIndex in QuestionView`() {
        val service = GetMemoryQuizService(photoRepository, questionRepository)
        val photoId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val photo = MemoryPhoto(
            id = photoId,
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Test",
            photoUrl = "url",
            memoryLabel = "label",
        )
        photoRepository.save(photo)

        val questions = listOf(
            MemoryQuizQuestion(
                id = UUID.randomUUID(),
                memoryPhotoId = photoId,
                question = "Q1",
                options = listOf("A", "B"),
                correctOptionIndex = 1,
                sortOrder = 0,
            ),
        )
        questionRepository.saveAll(questions)

        val result = service.handle(
            GetMemoryQuizQuery.In(memoryPhotoId = photoId, ownerUserId = ownerId),
        )

        assertEquals(photo, result.photo)
        assertEquals(1, result.questions.size)
        assertEquals("Q1", result.questions[0].question)
        assertEquals(listOf("A", "B"), result.questions[0].options)
    }

    @Test
    fun `GetTodayQuizService returns qualifying photo without today attempt`() {
        val service = GetTodayQuizService(photoRepository, questionRepository, attemptRepository)
        val ownerId = UUID.randomUUID()
        val photo1 = MemoryPhoto(
            id = UUID.randomUUID(),
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Photo 1 (attempted today)",
            photoUrl = "url1",
            memoryLabel = "label1",
            status = MemoryPhotoStatus.QUIZ_ACTIVE,
            createdAt = Instant.now().minusSeconds(100),
        )
        val photo2 = MemoryPhoto(
            id = UUID.randomUUID(),
            ownerUserId = ownerId,
            uploadedByUserId = UUID.randomUUID(),
            title = "Photo 2 (not attempted today)",
            photoUrl = "url2",
            memoryLabel = "label2",
            status = MemoryPhotoStatus.QUIZ_ACTIVE,
            createdAt = Instant.now().minusSeconds(200),
        )
        photoRepository.save(photo1)
        photoRepository.save(photo2)

        attemptRepository.save(
            MemoryQuizAttempt(
                memoryPhotoId = photo1.id,
                respondentUserId = UUID.randomUUID(),
                correctCount = 1,
                totalCount = 1,
                completedAt = Instant.now(),
            ),
        )

        val question = MemoryQuizQuestion(
            id = UUID.randomUUID(),
            memoryPhotoId = photo2.id,
            question = "Q2",
            options = listOf("Opt1", "Opt2"),
            correctOptionIndex = 0,
        )
        questionRepository.saveAll(listOf(question))

        val result = service.handle(GetTodayQuizQuery.In(ownerUserId = ownerId))

        assertNotNull(result.photo)
        assertEquals(photo2.id, result.photo?.id)
        assertEquals(1, result.questions.size)
        assertEquals("Q2", result.questions[0].question)
    }

    @Test
    fun `GetTodayQuizService returns null photo if none qualify`() {
        val service = GetTodayQuizService(photoRepository, questionRepository, attemptRepository)
        val ownerId = UUID.randomUUID()

        val result = service.handle(GetTodayQuizQuery.In(ownerUserId = ownerId))

        assertNull(result.photo)
        assertEquals(0, result.questions.size)
    }

    @Test
    fun `GetTodayQuizService picks the same photo on repeated calls the same day`() {
        val service = GetTodayQuizService(photoRepository, questionRepository, attemptRepository)
        val ownerId = UUID.randomUUID()
        val photos = (1..5).map {
            MemoryPhoto(
                id = UUID.randomUUID(),
                ownerUserId = ownerId,
                uploadedByUserId = ownerId,
                title = "Photo $it",
                photoUrl = "url$it",
                memoryLabel = "label$it",
                status = MemoryPhotoStatus.QUIZ_ACTIVE,
            )
        }
        photos.forEach { photoRepository.save(it) }

        val firstCall = service.handle(GetTodayQuizQuery.In(ownerUserId = ownerId))
        val secondCall = service.handle(GetTodayQuizQuery.In(ownerUserId = ownerId))

        assertNotNull(firstCall.photo)
        assertEquals(firstCall.photo?.id, secondCall.photo?.id)
    }

    @Test
    fun `quiz services reject a photo owned by another family`() {
        val photoId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val outsiderId = UUID.randomUUID()
        photoRepository.save(
            MemoryPhoto(
                id = photoId,
                ownerUserId = ownerId,
                uploadedByUserId = ownerId,
                title = "Test",
                photoUrl = "url",
                memoryLabel = "label",
            ),
        )
        questionRepository.saveAll(
            listOf(
                MemoryQuizQuestion(
                    id = UUID.randomUUID(),
                    memoryPhotoId = photoId,
                    question = "Q1",
                    options = listOf("A", "B"),
                    correctOptionIndex = 0,
                    sortOrder = 0,
                ),
            ),
        )

        assertThrows<ForbiddenException> {
            CreateMemoryQuizService(photoRepository, questionRepository).handle(
                CreateMemoryQuizCommand.In(
                    memoryPhotoId = photoId,
                    questions = listOf(
                        CreateMemoryQuizCommand.QuestionIn(
                            question = "Injected",
                            options = listOf("A", "B"),
                            correctOptionIndex = 0,
                        ),
                    ),
                    ownerUserId = outsiderId,
                ),
            )
        }

        assertThrows<ForbiddenException> {
            GetMemoryQuizService(photoRepository, questionRepository).handle(
                GetMemoryQuizQuery.In(memoryPhotoId = photoId, ownerUserId = outsiderId),
            )
        }

        assertThrows<ForbiddenException> {
            SubmitMemoryQuizAttemptService(photoRepository, questionRepository, attemptRepository).handle(
                SubmitMemoryQuizAttemptCommand.In(
                    memoryPhotoId = photoId,
                    respondentUserId = outsiderId,
                    answers = listOf(0),
                    ownerUserId = outsiderId,
                ),
            )
        }

        assertEquals(1, questionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(photoId).size)
        assertEquals(0, attemptRepository.attempts.size)
    }
}
