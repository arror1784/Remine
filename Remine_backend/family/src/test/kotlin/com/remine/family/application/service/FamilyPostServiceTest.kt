package com.remine.family.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.family.application.port.inbound.CreateFamilyPostCommand
import com.remine.family.application.port.inbound.CreateFamilyPostReplyCommand
import com.remine.family.application.port.inbound.GetFamilyFeedQuery
import com.remine.family.application.port.inbound.GetFamilyPostRepliesQuery
import com.remine.family.application.port.inbound.ToggleFamilyPostLikeCommand
import com.remine.family.application.port.outbound.FamilyPostLikeRepositoryPort
import com.remine.family.application.port.outbound.FamilyPostReplyRepositoryPort
import com.remine.family.application.port.outbound.FamilyPostRepositoryPort
import com.remine.family.domain.FamilyPost
import com.remine.family.domain.FamilyPostLike
import com.remine.family.domain.FamilyPostReply
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class FamilyPostServiceTest {

    private lateinit var postRepository: FakePostRepository
    private lateinit var likeRepository: FakeLikeRepository
    private lateinit var replyRepository: FakeReplyRepository
    private lateinit var service: FamilyPostService

    @BeforeEach
    fun setUp() {
        postRepository = FakePostRepository()
        likeRepository = FakeLikeRepository()
        replyRepository = FakeReplyRepository()
        service = FamilyPostService(postRepository, likeRepository, replyRepository)
    }

    @Test
    fun `create post saves and returns post`() {
        val authorId = UUID.randomUUID()
        val command = CreateFamilyPostCommand.In(
            authorUserId = authorId,
            body = "Hello Family!",
            photoUrl = "https://example.com/photo.jpg",
            photoCaption = "Family trip",
        )

        val result = service.handle(command)

        assertNotNull(result.entity.id)
        assertEquals(authorId, result.entity.authorUserId)
        assertEquals("Hello Family!", result.entity.body)
        assertEquals("https://example.com/photo.jpg", result.entity.photoUrl)
        assertEquals("Family trip", result.entity.photoCaption)
        assertEquals(0, result.entity.likeCount)
    }

    @Test
    fun `toggle like adds like when not present`() {
        val authorId = UUID.randomUUID()
        val viewerId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = authorId, body = "Test post"))

        val result = service.handle(
            ToggleFamilyPostLikeCommand.In(postId = post.id, userId = viewerId, pairUserIds = setOf(authorId, viewerId)),
        )

        assertTrue(result.liked)
        assertEquals(1, result.likeCount)
        assertNotNull(likeRepository.findByPostIdAndUserId(post.id, viewerId))
    }

    @Test
    fun `toggle like removes like when already liked`() {
        val authorId = UUID.randomUUID()
        val viewerId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = authorId, body = "Test post", likeCount = 1))
        likeRepository.save(FamilyPostLike(postId = post.id, userId = viewerId))

        val result = service.handle(
            ToggleFamilyPostLikeCommand.In(postId = post.id, userId = viewerId, pairUserIds = setOf(authorId, viewerId)),
        )

        assertFalse(result.liked)
        assertEquals(0, result.likeCount)
        assertNull(likeRepository.findByPostIdAndUserId(post.id, viewerId))
    }

    @Test
    fun `toggle like throws EntityNotFoundException when post does not exist`() {
        val nonExistentPostId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        assertThrows<EntityNotFoundException> {
            service.handle(
                ToggleFamilyPostLikeCommand.In(
                    postId = nonExistentPostId,
                    userId = userId,
                    pairUserIds = setOf(userId),
                ),
            )
        }
    }

    @Test
    fun `create reply saves reply when post exists`() {
        val authorId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = authorId, body = "Test post"))

        val result = service.handle(
            CreateFamilyPostReplyCommand.In(
                postId = post.id,
                authorUserId = authorId,
                body = "Great memory!",
                pairUserIds = setOf(authorId),
            )
        )

        assertNotNull(result.entity.id)
        assertEquals(post.id, result.entity.postId)
        assertEquals(authorId, result.entity.authorUserId)
        assertEquals("Great memory!", result.entity.body)
    }

    @Test
    fun `create reply throws EntityNotFoundException when post does not exist`() {
        assertThrows<EntityNotFoundException> {
            service.handle(
                CreateFamilyPostReplyCommand.In(
                    postId = UUID.randomUUID(),
                    authorUserId = UUID.randomUUID(),
                    body = "Reply to nowhere",
                    pairUserIds = setOf(UUID.randomUUID()),
                )
            )
        }
    }

    @Test
    fun `get feed returns posts with viewer state and reply count`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val pairIds = setOf(parentId, childId)

        val post1 = postRepository.save(FamilyPost(authorUserId = parentId, body = "Parent post", likeCount = 1))
        val post2 = postRepository.save(FamilyPost(authorUserId = childId, body = "Child post", likeCount = 0))

        likeRepository.save(FamilyPostLike(postId = post1.id, userId = childId))
        replyRepository.save(FamilyPostReply(postId = post1.id, authorUserId = childId, body = "Reply 1"))
        replyRepository.save(FamilyPostReply(postId = post1.id, authorUserId = parentId, body = "Reply 2"))

        val result = service.handle(
            GetFamilyFeedQuery.In(
                pairUserIds = pairIds,
                viewerUserId = childId,
                limit = 10,
            )
        )

        assertEquals(2, result.items.size)

        val item1 = result.items.first { it.post.id == post1.id }
        assertTrue(item1.likedByViewer)
        assertEquals(2, item1.replyCount)

        val item2 = result.items.first { it.post.id == post2.id }
        assertFalse(item2.likedByViewer)
        assertEquals(0, item2.replyCount)
    }

    @Test
    fun `get feed returns empty list when pairUserIds is empty`() {
        val result = service.handle(
            GetFamilyFeedQuery.In(
                pairUserIds = emptySet(),
                viewerUserId = UUID.randomUUID(),
            )
        )
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `get replies returns replies for post`() {
        val authorId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = authorId, body = "Test"))
        replyRepository.save(FamilyPostReply(postId = post.id, authorUserId = UUID.randomUUID(), body = "First"))
        replyRepository.save(FamilyPostReply(postId = post.id, authorUserId = UUID.randomUUID(), body = "Second"))

        val result = service.handle(
            GetFamilyPostRepliesQuery.In(postId = post.id, pairUserIds = setOf(authorId)),
        )

        assertEquals(2, result.items.size)
        assertEquals("First", result.items[0].body)
        assertEquals("Second", result.items[1].body)
    }

    @Test
    fun `toggle like throws ForbiddenException when post belongs to another family`() {
        val outsiderId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = UUID.randomUUID(), body = "Other family's post"))

        assertThrows<ForbiddenException> {
            service.handle(
                ToggleFamilyPostLikeCommand.In(
                    postId = post.id,
                    userId = outsiderId,
                    pairUserIds = setOf(outsiderId),
                ),
            )
        }
        assertNull(likeRepository.findByPostIdAndUserId(post.id, outsiderId))
    }

    @Test
    fun `create reply throws ForbiddenException when post belongs to another family`() {
        val outsiderId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = UUID.randomUUID(), body = "Other family's post"))

        assertThrows<ForbiddenException> {
            service.handle(
                CreateFamilyPostReplyCommand.In(
                    postId = post.id,
                    authorUserId = outsiderId,
                    body = "Intruding",
                    pairUserIds = setOf(outsiderId),
                ),
            )
        }
        assertTrue(replyRepository.findByPostIdOrderByCreatedAtAsc(post.id).isEmpty())
    }

    @Test
    fun `get replies throws ForbiddenException when post belongs to another family`() {
        val outsiderId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = UUID.randomUUID(), body = "Other family's post"))
        replyRepository.save(FamilyPostReply(postId = post.id, authorUserId = UUID.randomUUID(), body = "Private"))

        assertThrows<ForbiddenException> {
            service.handle(
                GetFamilyPostRepliesQuery.In(postId = post.id, pairUserIds = setOf(outsiderId)),
            )
        }
    }

    @Test
    fun `paired counterpart may act on a post authored by the other side of the pair`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val post = postRepository.save(FamilyPost(authorUserId = parentId, body = "Parent post"))

        val result = service.handle(
            ToggleFamilyPostLikeCommand.In(
                postId = post.id,
                userId = childId,
                pairUserIds = setOf(childId, parentId),
            ),
        )

        assertTrue(result.liked)
    }

    private class FakePostRepository : FamilyPostRepositoryPort {
        private val posts = mutableMapOf<UUID, FamilyPost>()

        override fun save(post: FamilyPost): FamilyPost {
            posts[post.id] = post
            return post
        }

        override fun findById(id: UUID): FamilyPost? = posts[id]

        override fun findFeed(pairUserIds: Set<UUID>, cursor: Instant?, limit: Int): List<FamilyPost> {
            return posts.values
                .filter { it.authorUserId in pairUserIds }
                .filter { cursor == null || it.createdAt.isBefore(cursor) }
                .sortedByDescending { it.createdAt }
                .take(limit)
        }

        override fun existsById(id: UUID): Boolean = posts.containsKey(id)

        override fun deleteAllByAuthorUserId(authorUserId: UUID) {
            posts.values.removeIf { it.authorUserId == authorUserId }
        }
    }

    private class FakeLikeRepository : FamilyPostLikeRepositoryPort {
        private val likes = mutableMapOf<UUID, FamilyPostLike>()

        override fun save(like: FamilyPostLike): FamilyPostLike {
            likes[like.id] = like
            return like
        }

        override fun findByPostIdAndUserId(postId: UUID, userId: UUID): FamilyPostLike? {
            return likes.values.firstOrNull { it.postId == postId && it.userId == userId }
        }

        override fun delete(like: FamilyPostLike) {
            likes.remove(like.id)
        }

        override fun findLikedPostIds(postIds: Collection<UUID>, userId: UUID): Set<UUID> {
            return likes.values
                .filter { it.userId == userId && it.postId in postIds }
                .map { it.postId }
                .toSet()
        }
    }

    private class FakeReplyRepository : FamilyPostReplyRepositoryPort {
        private val replies = mutableListOf<FamilyPostReply>()

        override fun save(reply: FamilyPostReply): FamilyPostReply {
            replies.add(reply)
            return reply
        }

        override fun findByPostIdOrderByCreatedAtAsc(postId: UUID): List<FamilyPostReply> {
            return replies.filter { it.postId == postId }.sortedBy { it.createdAt }
        }

        override fun countRepliesByPostIds(postIds: Collection<UUID>): Map<UUID, Int> {
            return replies
                .filter { it.postId in postIds }
                .groupingBy { it.postId }
                .eachCount()
        }
    }
}
