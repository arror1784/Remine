package com.remine.family.application.service

import com.remine.common.domain.exception.EntityNotFoundException
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
import com.remine.family.domain.PostWithViewerState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FamilyPostService(
    private val postRepository: FamilyPostRepositoryPort,
    private val likeRepository: FamilyPostLikeRepositoryPort,
    private val replyRepository: FamilyPostReplyRepositoryPort,
) : CreateFamilyPostCommand,
    ToggleFamilyPostLikeCommand,
    CreateFamilyPostReplyCommand,
    GetFamilyFeedQuery,
    GetFamilyPostRepliesQuery {

    @Transactional
    override fun handle(command: CreateFamilyPostCommand.In): CreateFamilyPostCommand.Out {
        val post = FamilyPost(
            authorUserId = command.authorUserId,
            body = command.body,
            photoUrl = command.photoUrl,
            photoCaption = command.photoCaption,
        )
        val saved = postRepository.save(post)
        return CreateFamilyPostCommand.Out(entity = saved)
    }

    @Transactional
    override fun handle(command: ToggleFamilyPostLikeCommand.In): ToggleFamilyPostLikeCommand.Out {
        val post = postRepository.findById(command.postId)
            ?: throw EntityNotFoundException("Family post not found with id: ${command.postId}")

        val existingLike = likeRepository.findByPostIdAndUserId(command.postId, command.userId)
        return if (existingLike != null) {
            likeRepository.delete(existingLike)
            val updatedPost = post.copy(likeCount = maxOf(0, post.likeCount - 1))
            val savedPost = postRepository.save(updatedPost)
            ToggleFamilyPostLikeCommand.Out(liked = false, likeCount = savedPost.likeCount)
        } else {
            val newLike = FamilyPostLike(
                postId = command.postId,
                userId = command.userId,
            )
            likeRepository.save(newLike)
            val updatedPost = post.copy(likeCount = post.likeCount + 1)
            val savedPost = postRepository.save(updatedPost)
            ToggleFamilyPostLikeCommand.Out(liked = true, likeCount = savedPost.likeCount)
        }
    }

    @Transactional
    override fun handle(command: CreateFamilyPostReplyCommand.In): CreateFamilyPostReplyCommand.Out {
        if (!postRepository.existsById(command.postId)) {
            throw EntityNotFoundException("Family post not found with id: ${command.postId}")
        }
        val reply = FamilyPostReply(
            postId = command.postId,
            authorUserId = command.authorUserId,
            body = command.body,
        )
        val saved = replyRepository.save(reply)
        return CreateFamilyPostReplyCommand.Out(entity = saved)
    }

    override fun handle(query: GetFamilyFeedQuery.In): GetFamilyFeedQuery.Out {
        if (query.pairUserIds.isEmpty()) {
            return GetFamilyFeedQuery.Out(items = emptyList())
        }

        val posts = postRepository.findFeed(
            pairUserIds = query.pairUserIds,
            cursor = query.cursor,
            limit = query.limit,
        )

        if (posts.isEmpty()) {
            return GetFamilyFeedQuery.Out(items = emptyList())
        }

        val postIds = posts.map { it.id }
        val likedPostIds = likeRepository.findLikedPostIds(postIds, query.viewerUserId)
        val replyCounts = replyRepository.countRepliesByPostIds(postIds)

        val items = posts.map { post ->
            PostWithViewerState(
                post = post,
                likedByViewer = likedPostIds.contains(post.id),
                replyCount = replyCounts[post.id] ?: 0,
            )
        }

        return GetFamilyFeedQuery.Out(items = items)
    }

    override fun handle(query: GetFamilyPostRepliesQuery.In): GetFamilyPostRepliesQuery.Out {
        if (!postRepository.existsById(query.postId)) {
            throw EntityNotFoundException("Family post not found with id: ${query.postId}")
        }
        val replies = replyRepository.findByPostIdOrderByCreatedAtAsc(query.postId)
        return GetFamilyPostRepliesQuery.Out(items = replies)
    }
}
