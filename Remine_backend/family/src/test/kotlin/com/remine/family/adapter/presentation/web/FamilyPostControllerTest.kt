package com.remine.family.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.family.application.port.inbound.CreateFamilyPostCommand
import com.remine.family.application.port.inbound.CreateFamilyPostReplyCommand
import com.remine.family.application.port.inbound.GetFamilyFeedQuery
import com.remine.family.application.port.inbound.GetFamilyPostRepliesQuery
import com.remine.family.application.port.inbound.ToggleFamilyPostLikeCommand
import com.remine.family.domain.FamilyPost
import com.remine.family.domain.FamilyPostReply
import com.remine.family.domain.PostWithViewerState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class FamilyPostControllerTest {

    private lateinit var createPostCommand: FakeCreatePostCommand
    private lateinit var toggleLikeCommand: FakeToggleLikeCommand
    private lateinit var createReplyCommand: FakeCreateReplyCommand
    private lateinit var getFeedQuery: FakeGetFeedQuery
    private lateinit var getRepliesQuery: FakeGetRepliesQuery
    private lateinit var controller: FamilyPostController

    @BeforeEach
    fun setUp() {
        createPostCommand = FakeCreatePostCommand()
        toggleLikeCommand = FakeToggleLikeCommand()
        createReplyCommand = FakeCreateReplyCommand()
        getFeedQuery = FakeGetFeedQuery()
        getRepliesQuery = FakeGetRepliesQuery()

        controller = FamilyPostController(
            createFamilyPostCommand = createPostCommand,
            toggleFamilyPostLikeCommand = toggleLikeCommand,
            createFamilyPostReplyCommand = createReplyCommand,
            getFamilyFeedQuery = getFeedQuery,
            getFamilyPostRepliesQuery = getRepliesQuery,
        )
    }

    @Test
    fun `createPost handles request and wraps response in ApiResponse`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = childId)
        val request = CreateFamilyPostRequest(body = "Hello world", photoUrl = "https://example.com/img.jpg")

        val response = controller.createPost(principal, request)

        assertNotNull(response.data)
        assertEquals("Hello world", response.data?.body)
        assertEquals("https://example.com/img.jpg", response.data?.photoUrl)
    }

    @Test
    fun `getFeed scopes feed to pair users`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = childId)

        val response = controller.getFeed(principal, null, 20)

        assertNotNull(response.data)
        assertEquals(1, response.data?.size)
        assertEquals(setOf(parentId, childId), getFeedQuery.lastPairUserIds)
    }

    @Test
    fun `toggleLike toggles post like`() {
        val userId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.CHILD, pairedUserId = UUID.randomUUID())
        val postId = UUID.randomUUID()

        val response = controller.toggleLike(principal, postId)

        assertNotNull(response.data)
        assertTrue(response.data?.liked == true)
        assertEquals(1, response.data?.likeCount)
    }

    @Test
    fun `createReply adds reply`() {
        val userId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.PARENT, pairedUserId = UUID.randomUUID())
        val postId = UUID.randomUUID()
        val request = CreateFamilyPostReplyRequest(body = "Nice picture!")

        val response = controller.createReply(principal, postId, request)

        assertNotNull(response.data)
        assertEquals("Nice picture!", response.data?.body)
    }

    @Test
    fun `getReplies returns replies`() {
        val userId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.PARENT, pairedUserId = UUID.randomUUID())
        val postId = UUID.randomUUID()

        val response = controller.getReplies(principal, postId)

        assertNotNull(response.data)
        assertEquals(1, response.data?.size)
    }

    private class FakeCreatePostCommand : CreateFamilyPostCommand {
        override fun handle(command: CreateFamilyPostCommand.In): CreateFamilyPostCommand.Out {
            return CreateFamilyPostCommand.Out(
                entity = FamilyPost(
                    authorUserId = command.authorUserId,
                    body = command.body,
                    photoUrl = command.photoUrl,
                    photoCaption = command.photoCaption,
                )
            )
        }
    }

    private class FakeToggleLikeCommand : ToggleFamilyPostLikeCommand {
        override fun handle(command: ToggleFamilyPostLikeCommand.In): ToggleFamilyPostLikeCommand.Out {
            return ToggleFamilyPostLikeCommand.Out(liked = true, likeCount = 1)
        }
    }

    private class FakeCreateReplyCommand : CreateFamilyPostReplyCommand {
        override fun handle(command: CreateFamilyPostReplyCommand.In): CreateFamilyPostReplyCommand.Out {
            return CreateFamilyPostReplyCommand.Out(
                entity = FamilyPostReply(
                    postId = command.postId,
                    authorUserId = command.authorUserId,
                    body = command.body,
                )
            )
        }
    }

    private class FakeGetFeedQuery : GetFamilyFeedQuery {
        var lastPairUserIds: Set<UUID>? = null

        override fun handle(query: GetFamilyFeedQuery.In): GetFamilyFeedQuery.Out {
            lastPairUserIds = query.pairUserIds
            return GetFamilyFeedQuery.Out(
                items = listOf(
                    PostWithViewerState(
                        post = FamilyPost(authorUserId = query.viewerUserId, body = "Feed post"),
                        likedByViewer = false,
                        replyCount = 0,
                    )
                )
            )
        }
    }

    private class FakeGetRepliesQuery : GetFamilyPostRepliesQuery {
        override fun handle(query: GetFamilyPostRepliesQuery.In): GetFamilyPostRepliesQuery.Out {
            return GetFamilyPostRepliesQuery.Out(
                items = listOf(
                    FamilyPostReply(
                        postId = query.postId,
                        authorUserId = UUID.randomUUID(),
                        body = "Sample reply",
                    )
                )
            )
        }
    }
}
