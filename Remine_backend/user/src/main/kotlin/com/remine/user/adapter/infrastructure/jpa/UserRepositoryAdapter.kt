package com.remine.user.adapter.infrastructure.jpa

import com.remine.user.application.port.outbound.UserRepositoryPort
import com.remine.user.domain.User
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserRepositoryPort {

    override fun save(user: User): User {
        val existing = userJpaRepository.findById(user.id).orElse(null)
        val entityToSave = if (existing != null) {
            existing.role = user.role
            existing.name = user.name
            existing.ageGroup = user.ageGroup
            existing.interests = if (user.interests.isNotEmpty()) user.interests.joinToString(",") else null
            existing.email = user.email
            existing.googleId = user.googleId
            existing.inviteCode = user.inviteCode
            existing.pairedUserId = user.pairedUserId
            existing.streakDays = user.streakDays
            existing
        } else {
            UserJpaEntity.fromDomain(user)
        }
        return userJpaRepository.save(entityToSave).toDomain()
    }

    override fun findById(id: UUID): User? {
        return userJpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByInviteCode(inviteCode: String): User? {
        return userJpaRepository.findByInviteCode(inviteCode)?.toDomain()
    }

    override fun existsByInviteCode(inviteCode: String): Boolean {
        return userJpaRepository.existsByInviteCode(inviteCode)
    }

    override fun findByGoogleId(googleId: String): User? {
        return userJpaRepository.findByGoogleId(googleId)?.toDomain()
    }
}
