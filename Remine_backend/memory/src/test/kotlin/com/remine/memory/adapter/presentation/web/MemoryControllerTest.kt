package com.remine.memory.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
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
import com.remine.memory.application.port.inbound.UploadMemoryPhotoImageCommand
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryQuizDraftQuestion
import com.remine.memory.domain.MemoryQuizQuestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.util.UUID

class MemoryControllerTest {

    private val parentId = UUID.randomUUID()
    private val childId = UUID.randomUUID()
    private val childPrincipal = RemineUserPrincipal(
        userId = childId,
        role = Role.CHILD,
        pairedUserId = parentId,
    )
    private val parentPrincipal = RemineUserPrincipal(
        userId = parentId,
        role = Role.PARENT,
        pairedUserId = childId,
    )

    @Test
    fun `uploadMemoryPhoto uses principal userId as uploadedBy and parentUserId as owner`() {
        var capturedIn: UploadMemoryPhotoCommand.In? = null
        val uploadCommand = object : UploadMemoryPhotoCommand {
            override fun handle(command: UploadMemoryPhotoCommand.In): UploadMemoryPhotoCommand.Out {
                capturedIn = command
                return UploadMemoryPhotoCommand.Out(
                    entity = MemoryPhoto(
                        id = UUID.randomUUID(),
                        ownerUserId = command.ownerUserId,
                        uploadedByUserId = command.uploadedByUserId,
                        title = command.title,
                        photoUrl = command.photoUrl,
                        memoryLabel = command.memoryLabel,
                    ),
                )
            }
        }

        val controller = createController(uploadCommand = uploadCommand)

        val response = controller.uploadMemoryPhoto(
            principal = childPrincipal,
            request = UploadMemoryPhotoRequest(
                title = "Trip",
                photoUrl = "https://example.com/img.png",
                memoryLabel = "2023년 가을",
            ),
        )

        assertEquals(childId, capturedIn?.uploadedByUserId)
        assertEquals(parentId, capturedIn?.ownerUserId)
        assertEquals("Trip", response.data?.title)
        assertNull(response.error)
    }

    @Test
    fun `uploadMemoryPhotoImage forwards the file bytes and returns the stored URL`() {
        var capturedIn: UploadMemoryPhotoImageCommand.In? = null
        val uploadImageCommand = object : UploadMemoryPhotoImageCommand {
            override fun handle(command: UploadMemoryPhotoImageCommand.In): UploadMemoryPhotoImageCommand.Out {
                capturedIn = command
                return UploadMemoryPhotoImageCommand.Out(url = "http://localhost:8080/uploads/memory-photos/new.png")
            }
        }
        val controller = createController(uploadImageCommand = uploadImageCommand)
        val file = MockMultipartFile("file", "cherry-blossom.png", "image/png", byteArrayOf(1, 2, 3))

        val response = controller.uploadMemoryPhotoImage(principal = childPrincipal, file = file)

        assertEquals("cherry-blossom.png", capturedIn?.originalFilename)
        assertEquals("image/png", capturedIn?.contentType)
        assertEquals(3, capturedIn?.bytes?.size)
        assertEquals("http://localhost:8080/uploads/memory-photos/new.png", response.data?.url)
        assertNull(response.error)
    }

    @Test
    fun `generateMemoryQuizQuestions passes parentUserId and photoId`() {
        var capturedIn: GenerateMemoryQuizQuestionsCommand.In? = null
        val photoId = UUID.randomUUID()
        val draftCommand = object : GenerateMemoryQuizQuestionsCommand {
            override fun handle(command: GenerateMemoryQuizQuestionsCommand.In): GenerateMemoryQuizQuestionsCommand.Out {
                capturedIn = command
                return GenerateMemoryQuizQuestionsCommand.Out(
                    questions = listOf(
                        MemoryQuizDraftQuestion(
                            id = UUID.randomUUID(),
                            memoryPhotoId = command.memoryPhotoId,
                            question = "이 사진은 어디서 찍었을까요?",
                            sortOrder = 0,
                        ),
                    ),
                )
            }
        }

        val controller = createController(generateDraftCommand = draftCommand)
        val response = controller.generateMemoryQuizQuestions(
            principal = childPrincipal,
            id = photoId,
        )

        assertEquals(photoId, capturedIn?.memoryPhotoId)
        assertEquals(parentId, capturedIn?.ownerUserId)
        assertEquals(1, response.data?.size)
        assertEquals("이 사진은 어디서 찍었을까요?", response.data?.get(0)?.question)
    }

    @Test
    fun `completeMemoryQuizWithAnswers passes answers and ownerId`() {
        var capturedIn: CompleteMemoryQuizWithAnswersCommand.In? = null
        val photoId = UUID.randomUUID()
        val questionId = UUID.randomUUID()
        val completeCommand = object : CompleteMemoryQuizWithAnswersCommand {
            override fun handle(command: CompleteMemoryQuizWithAnswersCommand.In): CompleteMemoryQuizWithAnswersCommand.Out {
                capturedIn = command
                return CompleteMemoryQuizWithAnswersCommand.Out(
                    questions = listOf(
                        MemoryQuizQuestion(
                            id = UUID.randomUUID(),
                            memoryPhotoId = command.memoryPhotoId,
                            question = "이 사진은 어디서 찍었을까요?",
                            options = listOf("제주도", "부산", "경주", "강릉"),
                            correctOptionIndex = 0,
                            sortOrder = 0,
                        ),
                    ),
                )
            }
        }

        val controller = createController(completeCommand = completeCommand)
        val response = controller.completeMemoryQuizWithAnswers(
            principal = childPrincipal,
            id = photoId,
            request = CompleteMemoryQuizWithAnswersRequest(
                answers = listOf(
                    DraftAnswerRequest(
                        questionId = questionId,
                        answer = "제주도",
                    ),
                ),
            ),
        )

        assertEquals(photoId, capturedIn?.memoryPhotoId)
        assertEquals(parentId, capturedIn?.ownerUserId)
        assertEquals(questionId, capturedIn?.answers?.get(0)?.draftQuestionId)
        assertEquals("제주도", capturedIn?.answers?.get(0)?.answer)
        assertEquals(1, response.data?.size)
        assertEquals(4, response.data?.get(0)?.options?.size)
    }

    @Test
    fun `getMemoryGallery returns list for parentUserId with attempted flag from the query result`() {
        var queriedOwnerId: UUID? = null
        val attemptedPhotoId = UUID.randomUUID()
        val unattemptedPhotoId = UUID.randomUUID()
        val galleryQuery = object : GetMemoryGalleryQuery {
            override fun handle(query: GetMemoryGalleryQuery.In): GetMemoryGalleryQuery.Out {
                queriedOwnerId = query.ownerUserId
                return GetMemoryGalleryQuery.Out(
                    items = listOf(
                        MemoryPhoto(
                            id = attemptedPhotoId,
                            ownerUserId = query.ownerUserId,
                            uploadedByUserId = childId,
                            title = "Photo 1",
                            photoUrl = "url",
                            memoryLabel = "label",
                        ),
                        MemoryPhoto(
                            id = unattemptedPhotoId,
                            ownerUserId = query.ownerUserId,
                            uploadedByUserId = childId,
                            title = "Photo 2",
                            photoUrl = "url",
                            memoryLabel = "label",
                        ),
                    ),
                    attemptedPhotoIds = setOf(attemptedPhotoId),
                )
            }
        }

        val controller = createController(galleryQuery = galleryQuery)
        val response = controller.getMemoryGallery(principal = parentPrincipal)

        assertEquals(parentId, queriedOwnerId)
        assertEquals(2, response.data?.size)
        assertEquals("Photo 1", response.data?.get(0)?.title)
        assertEquals(true, response.data?.get(0)?.attempted)
        assertEquals(false, response.data?.get(1)?.attempted)
    }

    @Test
    fun `submitQuizAttempt passes principal userId as respondent`() {
        var capturedAttemptIn: SubmitMemoryQuizAttemptCommand.In? = null
        val submitCommand = object : SubmitMemoryQuizAttemptCommand {
            override fun handle(command: SubmitMemoryQuizAttemptCommand.In): SubmitMemoryQuizAttemptCommand.Out {
                capturedAttemptIn = command
                return SubmitMemoryQuizAttemptCommand.Out(
                    correctCount = 2,
                    totalCount = 2,
                )
            }
        }

        val controller = createController(submitCommand = submitCommand)
        val photoId = UUID.randomUUID()
        val response = controller.submitQuizAttempt(
            principal = parentPrincipal,
            id = photoId,
            request = SubmitMemoryQuizAttemptRequest(answers = listOf(0, 1)),
        )

        assertEquals(photoId, capturedAttemptIn?.memoryPhotoId)
        assertEquals(parentId, capturedAttemptIn?.respondentUserId)
        assertEquals(listOf(0, 1), capturedAttemptIn?.answers)
        assertEquals(2, response.data?.correctCount)
        assertEquals(2, response.data?.totalCount)
    }

    private fun createController(
        uploadCommand: UploadMemoryPhotoCommand = mockUploadCommand(),
        uploadImageCommand: UploadMemoryPhotoImageCommand = mockUploadImageCommand(),
        galleryQuery: GetMemoryGalleryQuery = mockGetGalleryQuery(),
        statsQuery: GetMemoryStatsQuery = mockGetStatsQuery(),
        createQuizCommand: CreateMemoryQuizCommand = mockCreateQuizCommand(),
        generateDraftCommand: GenerateMemoryQuizQuestionsCommand = mockGenerateDraftCommand(),
        completeCommand: CompleteMemoryQuizWithAnswersCommand = mockCompleteCommand(),
        getDraftQuery: GetMemoryQuizDraftQuestionsQuery = mockGetDraftQuery(),
        quizQuery: GetMemoryQuizQuery = mockGetQuizQuery(),
        todayQuery: GetTodayQuizQuery = mockGetTodayQuizQuery(),
        submitCommand: SubmitMemoryQuizAttemptCommand = mockSubmitAttemptCommand(),
    ) = MemoryController(
        uploadMemoryPhotoCommand = uploadCommand,
        uploadMemoryPhotoImageCommand = uploadImageCommand,
        getMemoryGalleryQuery = galleryQuery,
        getMemoryStatsQuery = statsQuery,
        createMemoryQuizCommand = createQuizCommand,
        generateMemoryQuizQuestionsCommand = generateDraftCommand,
        completeMemoryQuizWithAnswersCommand = completeCommand,
        getMemoryQuizDraftQuestionsQuery = getDraftQuery,
        getMemoryQuizQuery = quizQuery,
        getTodayQuizQuery = todayQuery,
        submitMemoryQuizAttemptCommand = submitCommand,
    )

    private fun mockUploadImageCommand() = object : UploadMemoryPhotoImageCommand {
        override fun handle(command: UploadMemoryPhotoImageCommand.In): UploadMemoryPhotoImageCommand.Out =
            UploadMemoryPhotoImageCommand.Out(url = "https://example.com/uploads/memory-photos/stub.png")
    }

    private fun mockUploadCommand() = object : UploadMemoryPhotoCommand {
        override fun handle(command: UploadMemoryPhotoCommand.In): UploadMemoryPhotoCommand.Out =
            UploadMemoryPhotoCommand.Out(
                entity = MemoryPhoto(
                    ownerUserId = command.ownerUserId,
                    uploadedByUserId = command.uploadedByUserId,
                    title = command.title,
                    photoUrl = command.photoUrl,
                    memoryLabel = command.memoryLabel,
                ),
            )
    }

    private fun mockGetGalleryQuery() = object : GetMemoryGalleryQuery {
        override fun handle(query: GetMemoryGalleryQuery.In): GetMemoryGalleryQuery.Out =
            GetMemoryGalleryQuery.Out(items = emptyList())
    }

    private fun mockGetStatsQuery() = object : GetMemoryStatsQuery {
        override fun handle(query: GetMemoryStatsQuery.In): GetMemoryStatsQuery.Out =
            GetMemoryStatsQuery.Out(totalPhotos = 0, quizActiveCount = 0, addedThisMonth = 0)
    }

    private fun mockCreateQuizCommand() = object : CreateMemoryQuizCommand {
        override fun handle(command: CreateMemoryQuizCommand.In): CreateMemoryQuizCommand.Out =
            CreateMemoryQuizCommand.Out(questions = emptyList())
    }

    private fun mockGenerateDraftCommand() = object : GenerateMemoryQuizQuestionsCommand {
        override fun handle(command: GenerateMemoryQuizQuestionsCommand.In): GenerateMemoryQuizQuestionsCommand.Out =
            GenerateMemoryQuizQuestionsCommand.Out(questions = emptyList())
    }

    private fun mockCompleteCommand() = object : CompleteMemoryQuizWithAnswersCommand {
        override fun handle(command: CompleteMemoryQuizWithAnswersCommand.In): CompleteMemoryQuizWithAnswersCommand.Out =
            CompleteMemoryQuizWithAnswersCommand.Out(questions = emptyList())
    }

    private fun mockGetDraftQuery() = object : GetMemoryQuizDraftQuestionsQuery {
        override fun handle(query: GetMemoryQuizDraftQuestionsQuery.In): GetMemoryQuizDraftQuestionsQuery.Out =
            GetMemoryQuizDraftQuestionsQuery.Out(questions = emptyList())
    }

    private fun mockGetQuizQuery() = object : GetMemoryQuizQuery {
        override fun handle(query: GetMemoryQuizQuery.In): GetMemoryQuizQuery.Out =
            GetMemoryQuizQuery.Out(
                photo = MemoryPhoto(
                    id = query.memoryPhotoId,
                    ownerUserId = UUID.randomUUID(),
                    uploadedByUserId = UUID.randomUUID(),
                    title = "T",
                    photoUrl = "u",
                    memoryLabel = "l",
                ),
                questions = emptyList(),
            )
    }

    private fun mockGetTodayQuizQuery() = object : GetTodayQuizQuery {
        override fun handle(query: GetTodayQuizQuery.In): GetTodayQuizQuery.Out =
            GetTodayQuizQuery.Out(photo = null, questions = emptyList())
    }

    private fun mockSubmitAttemptCommand() = object : SubmitMemoryQuizAttemptCommand {
        override fun handle(command: SubmitMemoryQuizAttemptCommand.In): SubmitMemoryQuizAttemptCommand.Out =
            SubmitMemoryQuizAttemptCommand.Out(correctCount = 0, totalCount = 0)
    }
}
