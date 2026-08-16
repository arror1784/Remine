package com.remine.user.domain

import com.remine.auth.domain.Role
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val role: Role,
    val name: String,
    val ageGroup: String,
    val interests: List<String> = emptyList(),
    val email: String? = null,
    val googleId: String? = null,
    val inviteCode: String? = null,
    val pairedUserId: UUID? = null,
    val streakDays: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    fun updateProfile(
        name: String? = null,
        ageGroup: String? = null,
        interests: List<String>? = null,
    ): User {
        return this.copy(
            name = name ?: this.name,
            ageGroup = ageGroup ?: this.ageGroup,
            interests = if (interests != null && this.role == Role.PARENT) interests else this.interests,
            updatedAt = Instant.now(),
        )
    }

    fun pairWith(targetUserId: UUID): User {
        return this.copy(
            pairedUserId = targetUserId,
            updatedAt = Instant.now(),
        )
    }
}
