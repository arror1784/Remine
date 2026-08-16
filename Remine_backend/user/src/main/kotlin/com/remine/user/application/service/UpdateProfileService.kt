package com.remine.user.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.user.application.port.inbound.UpdateProfileCommand
import com.remine.user.application.port.outbound.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateProfileService(
    private val userRepositoryPort: UserRepositoryPort,
) : UpdateProfileCommand {

    override fun handle(command: UpdateProfileCommand.In): UpdateProfileCommand.Out {
        val user = userRepositoryPort.findById(command.userId)
            ?: throw EntityNotFoundException("User not found: ${command.userId}")

        if (command.ageGroup != null && command.ageGroup !in SignUpService.VALID_AGE_GROUPS) {
            throw InvalidRequestException("Invalid age group: ${command.ageGroup}. Must be one of: ${SignUpService.VALID_AGE_GROUPS}")
        }

        val updatedUser = user.updateProfile(
            name = command.name,
            ageGroup = command.ageGroup,
            interests = command.interests?.map { it.trim() }?.filter { it.isNotEmpty() },
        )

        val savedUser = userRepositoryPort.save(updatedUser)
        return UpdateProfileCommand.Out(entity = savedUser)
    }
}
