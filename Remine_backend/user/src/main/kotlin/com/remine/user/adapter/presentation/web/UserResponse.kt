package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.Role
import com.remine.user.domain.User
import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val role: Role,
    val name: String,
    val ageGroup: String,
    val interests: List<String>,
    val email: String?,
    val inviteCode: String?,
    val pairedUserId: UUID?,
    val streakDays: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id,
            role = user.role,
            name = user.name,
            ageGroup = user.ageGroup,
            interests = user.interests,
            email = user.email,
            inviteCode = user.inviteCode,
            pairedUserId = user.pairedUserId,
            streakDays = user.streakDays,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }
}
