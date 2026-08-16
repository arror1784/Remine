package com.remine.user.adapter.infrastructure.jpa

import com.remine.auth.domain.Role
import com.remine.common.persistence.BaseOrmEntity
import com.remine.user.domain.User
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "app_user")
@Where(clause = "deleted_at IS NULL")
class UserJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    var role: Role,

    @Column(name = "name", nullable = false, length = 50)
    var name: String,

    @Column(name = "age_group", nullable = false, length = 10)
    var ageGroup: String,

    @Column(name = "interests", length = 255)
    var interests: String? = null,

    @Column(name = "email", length = 255)
    var email: String? = null,

    @Column(name = "google_id", length = 255)
    var googleId: String? = null,

    @Column(name = "invite_code", length = 20)
    var inviteCode: String? = null,

    @Column(name = "paired_user_id", columnDefinition = "uuid")
    var pairedUserId: UUID? = null,

    @Column(name = "streak_days", nullable = false)
    var streakDays: Int = 0,
) : BaseOrmEntity(id) {

    fun toDomain(): User {
        val parsedInterests = interests
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return User(
            id = id,
            role = role,
            name = name,
            ageGroup = ageGroup,
            interests = parsedInterests,
            email = email,
            googleId = googleId,
            inviteCode = inviteCode,
            pairedUserId = pairedUserId,
            streakDays = streakDays,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromDomain(user: User): UserJpaEntity {
            val joinedInterests = if (user.interests.isNotEmpty()) {
                user.interests.joinToString(",")
            } else {
                null
            }

            return UserJpaEntity(
                id = user.id,
                role = user.role,
                name = user.name,
                ageGroup = user.ageGroup,
                interests = joinedInterests,
                email = user.email,
                googleId = user.googleId,
                inviteCode = user.inviteCode,
                pairedUserId = user.pairedUserId,
                streakDays = user.streakDays,
            )
        }
    }
}
