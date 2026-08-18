package com.remine.user.application.service

import com.remine.auth.domain.Role
import com.remine.auth.jwt.JwtTokenProvider
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.user.application.port.inbound.DemoLoginCommand
import com.remine.user.application.port.outbound.UserRepositoryPort
import com.remine.user.domain.DemoVariant
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
        val demoUserId = userIdFor(command.role, command.variant)

        val user = userRepositoryPort.findById(demoUserId)
            ?: throw EntityNotFoundException(
                "Demo ${command.role}/${command.variant} account $demoUserId not found. " +
                    "Has ${migrationFileFor(command.variant)} been applied?",
            )

        return DemoLoginCommand.Out(
            userId = user.id,
            role = user.role,
            name = user.name,
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

        // Must stay in sync with migration/src/main/resources/db/migration/V13__seed_demo_variant_users.sql
        // Also referenced directly by DemoResetService (app-api) to scope its wipe/reseed to
        // this pair only — it must never see or touch the EVAL pair above.
        val SHOW_PARENT_ID: UUID = UUID.fromString("7b2f4b0a-6e6c-4f3d-9c1a-2f6a5e9d7c31")
        val SHOW_CHILD_ID: UUID = UUID.fromString("d4a8c6f2-1b3e-4a5d-8f7c-3e9b2a6d4f18")

        fun userIdFor(role: Role, variant: DemoVariant): UUID = when (variant) {
            DemoVariant.EVAL -> when (role) {
                Role.PARENT -> DEMO_PARENT_ID
                Role.CHILD -> DEMO_CHILD_ID
            }
            DemoVariant.DEMO -> when (role) {
                Role.PARENT -> SHOW_PARENT_ID
                Role.CHILD -> SHOW_CHILD_ID
            }
        }

        private fun migrationFileFor(variant: DemoVariant): String = when (variant) {
            DemoVariant.EVAL -> "V8__seed_demo_users.sql"
            DemoVariant.DEMO -> "V13__seed_demo_variant_users.sql"
        }
    }
}
