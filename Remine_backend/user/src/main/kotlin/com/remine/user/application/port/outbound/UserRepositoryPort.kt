package com.remine.user.application.port.outbound

import com.remine.user.domain.User
import java.util.UUID

interface UserRepositoryPort {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByInviteCode(inviteCode: String): User?
    fun existsByInviteCode(inviteCode: String): Boolean
    fun findByGoogleId(googleId: String): User?
}
