package com.remine.message.application.port.outbound

import com.remine.message.domain.QuickReply

interface QuickReplyRepositoryPort {
    fun findByRole(role: String): List<QuickReply>
    fun existsByRole(role: String): Boolean
    fun save(quickReply: QuickReply): QuickReply
    fun saveAll(quickReplies: List<QuickReply>): List<QuickReply>
}
