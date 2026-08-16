package com.remine.user.application.service

import com.remine.auth.domain.Role
import com.remine.auth.jwt.JwtTokenProvider
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.user.application.port.inbound.DemoLoginCommand
import com.remine.user.application.port.outbound.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DemoLoginService(
    private val userRepositoryPort: UserRepositoryPort,
    private val jwtTokenProvider: JwtTokenProvider,
) : DemoLoginCommand {

    override fun handle(command: DemoLoginCommand.In): DemoLoginCommand.Out {
        val demoUserId = when (command.role) {
            Role.PARENT -> DEMO_PARENT_ID
            Role.CHILD -> DEMO_CHILD_ID
        }

        val user = userRepositoryPort.findById(demoUserId)
            ?: throw EntityNotFoundException(
                "Demo ${command.role} account $demoUserId not found. Has V8__seed_demo_users.sql been applied?",
            )

        return DemoLoginCommand.Out(
            userId = user.id,
            role = user.role,
            accessToken = jwtTokenProvider.generateToken(
                userId = user.id,
                role = user.role,
                pairedUserId = user.pairedUserId,
            ),
            pairedUserId = user.pairedUserId,
        )
    }

    companion object {
        // Must stay in sync with migration/src/main/resources/db/migration/V8__seed_demo_users.sql
        val DEMO_PARENT_ID: UUID = UUID.fromString("1c77b040-9278-4a22-adb1-0345ab254551")
        val DEMO_CHILD_ID: UUID = UUID.fromString("01421a39-6467-465c-a6e5-8e3007225296")
    }
}
