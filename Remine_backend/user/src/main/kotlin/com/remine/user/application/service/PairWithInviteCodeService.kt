package com.remine.user.application.service

import com.remine.auth.jwt.JwtTokenProvider
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.user.application.port.inbound.PairWithInviteCodeCommand
import com.remine.user.application.port.outbound.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PairWithInviteCodeService(
    private val userRepositoryPort: UserRepositoryPort,
    private val jwtTokenProvider: JwtTokenProvider,
) : PairWithInviteCodeCommand {

    override fun handle(command: PairWithInviteCodeCommand.In): PairWithInviteCodeCommand.Out {
        val parent = userRepositoryPort.findByInviteCode(command.inviteCode)
            ?: throw EntityNotFoundException("Parent user with invite code '${command.inviteCode}' not found")

        val child = userRepositoryPort.findById(command.childUserId)
            ?: throw EntityNotFoundException("Child user not found: ${command.childUserId}")

        if (parent.id == child.id) {
            throw InvalidRequestException("Cannot pair with self")
        }

        val updatedParent = parent.pairWith(child.id)
        val updatedChild = child.pairWith(parent.id)

        userRepositoryPort.save(updatedParent)
        val savedChild = userRepositoryPort.save(updatedChild)

        val accessToken = jwtTokenProvider.generateToken(
            userId = savedChild.id,
            role = savedChild.role,
            pairedUserId = savedChild.pairedUserId,
        )

        return PairWithInviteCodeCommand.Out(
            parentUserId = parent.id,
            accessToken = accessToken,
        )
    }
}
