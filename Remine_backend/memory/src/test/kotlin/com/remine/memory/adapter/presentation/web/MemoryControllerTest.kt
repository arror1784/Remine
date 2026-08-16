package com.remine.memory.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.memory.application.port.inbound.CreateMemoryQuizCommand
import com.remine.memory.application.port.inbound.GetMemoryGalleryQuery
import com.remine.memory.application.port.inbound.GetMemoryQuizQuery
import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.memory.application.port.inbound.GetTodayQuizQuery
import com.remine.memory.application.port.inbound.SubmitMemoryQuizAttemptCommand
import com.remine.memory.application.port.inbound.UploadMemoryPhotoCommand
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizQuestion
import com.remine.memory.domain.QuestionView
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
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

        val controller = MemoryController(
            uploadMemoryPhotoCommand = uploadCommand,
            getMemoryGalleryQuery = mockGetGalleryQuery(),
            getMemoryStatsQuery = mockGetStatsQuery(),
            createMemoryQuizCommand = mockCreateQuizCommand(),
            getMemoryQuizQuery = mockGetQuizQuery(),
            getTodayQuizQuery = mockGetTodayQuizQuery(),
            submitMemoryQuizAttemptCommand = mockSubmitAttemptCommand(),
        )

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
    fun `getMemoryGallery returns list for parentUserId`() {
        var queriedOwnerId: UUID? = null
        val galleryQuery = object : GetMemoryGalleryQuery {
            override fun handle(query: GetMemoryGalleryQuery.In): GetMemoryGalleryQuery.Out {
                queriedOwnerId = query.ownerUserId
                return GetMemoryGalleryQuery.Out(
                    items = listOf(
                        MemoryPhoto(
                            id = UUID.randomUUID(),
                            ownerUserId = query.ownerUserId,
                            uploadedByUserId = childId,
                            title = "Photo 1",
                            photoUrl = "url",
                            memoryLabel = "label",
                        ),
                    ),
                )
            }
        }

        val controller = MemoryController(
            uploadMemoryPhotoCommand = mockUploadCommand(),
            getMemoryGalleryQuery = galleryQuery,
            getMemoryStatsQuery = mockGetStatsQuery(),
            createMemoryQuizCommand = mockCreateQuizCommand(),
            getMemoryQuizQuery = mockGetQuizQuery(),
            getTodayQuizQuery = mockGetTodayQuizQuery(),
            submitMemoryQuizAttemptCommand = mockSubmitAttemptCommand(),
        )

        val response = controller.getMemoryGallery(principal = parentPrincipal)

        assertEquals(parentId, queriedOwnerId)
        assertEquals(1, response.data?.size)
        assertEquals("Photo 1", response.data?.get(0)?.title)
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

        val controller = MemoryController(
            uploadMemoryPhotoCommand = mockUploadCommand(),
            getMemoryGalleryQuery = mockGetGalleryQuery(),
            getMemoryStatsQuery = mockGetStatsQuery(),
            createMemoryQuizCommand = mockCreateQuizCommand(),
            getMemoryQuizQuery = mockGetQuizQuery(),
            getTodayQuizQuery = mockGetTodayQuizQuery(),
            submitMemoryQuizAttemptCommand = submitCommand,
        )

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
