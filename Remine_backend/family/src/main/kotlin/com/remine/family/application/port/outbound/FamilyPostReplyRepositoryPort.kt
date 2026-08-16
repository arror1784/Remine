package com.remine.family.application.port.outbound

import com.remine.family.domain.FamilyPostReply
import java.util.UUID

interface FamilyPostReplyRepositoryPort {
    fun save(reply: FamilyPostReply): FamilyPostReply
    fun findByPostIdOrderByCreatedAtAsc(postId: UUID): List<FamilyPostReply>
    fun countRepliesByPostIds(postIds: Collection<UUID>): Map<UUID, Int>
}
