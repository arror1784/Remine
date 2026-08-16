package com.remine.family.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.common.web.ApiResponse
import com.remine.family.application.port.inbound.CreateFamilyPostCommand
import com.remine.family.application.port.inbound.CreateFamilyPostReplyCommand
import com.remine.family.application.port.inbound.GetFamilyFeedQuery
import com.remine.family.application.port.inbound.GetFamilyPostRepliesQuery
import com.remine.family.application.port.inbound.ToggleFamilyPostLikeCommand
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/family/posts")
class FamilyPostController(
    private val createFamilyPostCommand: CreateFamilyPostCommand,
    private val toggleFamilyPostLikeCommand: ToggleFamilyPostLikeCommand,
    private val createFamilyPostReplyCommand: CreateFamilyPostReplyCommand,
    private val getFamilyFeedQuery: GetFamilyFeedQuery,
    private val getFamilyPostRepliesQuery: GetFamilyPostRepliesQuery,
) {

    @PostMapping
    fun createPost(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: CreateFamilyPostRequest,
    ): ApiResponse<FamilyPostResponse> {
        val result = createFamilyPostCommand.handle(
            CreateFamilyPostCommand.In(
                authorUserId = principal.userId,
                body = request.body,
                photoUrl = request.photoUrl,
                photoCaption = request.photoCaption,
            )
        )
        return ApiResponse.ok(FamilyPostResponse.from(result.entity))
    }

    @GetMapping
    fun getFeed(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @RequestParam(name = "cursor", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        cursor: Instant?,
        @RequestParam(name = "limit", required = false, defaultValue = "20")
        limit: Int,
    ): ApiResponse<List<PostWithViewerStateResponse>> {
        val result = getFamilyFeedQuery.handle(
            GetFamilyFeedQuery.In(
                pairUserIds = resolvePairUserIds(principal),
                viewerUserId = principal.userId,
                cursor = cursor,
                limit = limit.coerceIn(1, MAX_PAGE_SIZE),
            )
        )
        return ApiResponse.ok(result.items.map { PostWithViewerStateResponse.from(it) })
    }

    @PatchMapping("/{id}/like")
    fun toggleLike(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable("id") id: UUID,
    ): ApiResponse<ToggleLikeResponse> {
        val result = toggleFamilyPostLikeCommand.handle(
            ToggleFamilyPostLikeCommand.In(
                postId = id,
                userId = principal.userId,
                pairUserIds = resolvePairUserIds(principal),
            )
        )
        return ApiResponse.ok(ToggleLikeResponse.from(result))
    }

    @PostMapping("/{id}/replies")
    fun createReply(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable("id") id: UUID,
        @Valid @RequestBody request: CreateFamilyPostReplyRequest,
    ): ApiResponse<FamilyPostReplyResponse> {
        val result = createFamilyPostReplyCommand.handle(
            CreateFamilyPostReplyCommand.In(
                postId = id,
                authorUserId = principal.userId,
                body = request.body,
                pairUserIds = resolvePairUserIds(principal),
            )
        )
        return ApiResponse.ok(FamilyPostReplyResponse.from(result.entity))
    }

    @GetMapping("/{id}/replies")
    fun getReplies(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable("id") id: UUID,
    ): ApiResponse<List<FamilyPostReplyResponse>> {
        val result = getFamilyPostRepliesQuery.handle(
            GetFamilyPostRepliesQuery.In(
                postId = id,
                pairUserIds = resolvePairUserIds(principal),
            )
        )
        return ApiResponse.ok(result.items.map { FamilyPostReplyResponse.from(it) })
    }

    private fun resolvePairUserIds(principal: RemineUserPrincipal): Set<UUID> = setOfNotNull(
        principal.userId,
        runCatching { principal.parentUserId() }.getOrNull(),
        principal.counterpartUserId(),
    )

    private companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
