package com.remine.user.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserJpaEntity, UUID> {
    fun findByInviteCode(inviteCode: String): UserJpaEntity?
    fun existsByInviteCode(inviteCode: String): Boolean
    fun findByGoogleId(googleId: String): UserJpaEntity?
}
