package com.remine.family.application.port.outbound

import com.remine.family.domain.FamilyPost
import java.time.Instant
import java.util.UUID

interface FamilyPostRepositoryPort {
    fun save(post: FamilyPost): FamilyPost
    fun findById(id: UUID): FamilyPost?
    fun findFeed(pairUserIds: Set<UUID>, cursor: Instant?, limit: Int): List<FamilyPost>
    fun existsById(id: UUID): Boolean

    /** Used by the demo-reset utility (see app-api's DemoResetService) to wipe a demo account's posts. */
    fun deleteAllByAuthorUserId(authorUserId: UUID)
}
