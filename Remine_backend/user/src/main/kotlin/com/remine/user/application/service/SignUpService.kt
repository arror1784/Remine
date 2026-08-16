package com.remine.user.application.service

import com.remine.auth.domain.Role
import com.remine.auth.jwt.JwtTokenProvider
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.user.application.port.inbound.SignUpCommand
import com.remine.user.application.port.outbound.UserRepositoryPort
import com.remine.user.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom

@Service
@Transactional
class SignUpService(
    private val userRepositoryPort: UserRepositoryPort,
    private val jwtTokenProvider: JwtTokenProvider,
) : SignUpCommand {

    private val random = SecureRandom()
    private val charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    override fun handle(command: SignUpCommand.In): SignUpCommand.Out {
        if (command.ageGroup !in VALID_AGE_GROUPS) {
            throw InvalidRequestException("Invalid age group: ${command.ageGroup}. Must be one of: $VALID_AGE_GROUPS")
        }

        val inviteCode = if (command.role == Role.PARENT) {
            generateUniqueInviteCode()
        } else {
            null
        }

        val parsedInterests = if (command.role == Role.PARENT) {
            command.interests.map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        val user = User(
            role = command.role,
            name = command.name,
            ageGroup = command.ageGroup,
            interests = parsedInterests,
            email = command.email,
            googleId = command.googleId,
            inviteCode = inviteCode,
            pairedUserId = null,
        )

        val savedUser = userRepositoryPort.save(user)
        val accessToken = jwtTokenProvider.generateToken(
            userId = savedUser.id,
            role = savedUser.role,
            pairedUserId = savedUser.pairedUserId,
        )

        return SignUpCommand.Out(
            userId = savedUser.id,
            inviteCode = savedUser.inviteCode,
            accessToken = accessToken,
        )
    }

    private fun generateUniqueInviteCode(): String {
        var attempts = 0
        while (attempts < MAX_ATTEMPTS) {
            val code = "REMIND-" + (1..4)
                .map { charPool[random.nextInt(charPool.length)] }
                .joinToString("")

            if (!userRepositoryPort.existsByInviteCode(code)) {
                return code
            }
            attempts++
        }
        throw IllegalStateException("Failed to generate a unique invite code after $MAX_ATTEMPTS attempts")
    }

    companion object {
        private const val MAX_ATTEMPTS = 10
        val VALID_AGE_GROUPS = setOf("10대", "20대", "30대", "40대", "50대", "60대", "70대", "80대", "기타")
    }
}
