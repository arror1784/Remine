package com.remine.user.application.service

import com.remine.auth.domain.Role
import com.remine.auth.jwt.JwtTokenProvider
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.user.application.port.inbound.DemoLoginCommand
import com.remine.user.application.port.outbound.UserRepositoryPort
import com.remine.user.domain.DemoVariant
import com.remine.user.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class DemoLoginServiceTest {

    private val jwtTokenProvider =
        JwtTokenProvider("test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256", 604800000)

    @Test
    fun `demo login issues a token for the seeded eval parent account`() {
        val parent = User(
            id = DemoLoginService.DEMO_PARENT_ID,
            role = Role.PARENT,
            name = "데모 부모",
            ageGroup = "70대",
            interests = listOf("걷기·산책"),
            inviteCode = "REMIND-DEMO",
            pairedUserId = DemoLoginService.DEMO_CHILD_ID,
        )
        val service = DemoLoginService(StubUserRepository(mapOf(parent.id to parent)), jwtTokenProvider)

        val out = service.handle(DemoLoginCommand.In(role = Role.PARENT))

        assertEquals(DemoLoginService.DEMO_PARENT_ID, out.userId)
        assertEquals(Role.PARENT, out.role)
        assertEquals(DemoLoginService.DEMO_CHILD_ID, out.pairedUserId)
        assertNotNull(jwtTokenProvider.parse(out.accessToken))
    }

    @Test
    fun `variant defaults to EVAL so omitting it keeps existing behavior unchanged`() {
        val parent = User(
            id = DemoLoginService.DEMO_PARENT_ID,
            role = Role.PARENT,
            name = "데모 부모",
            ageGroup = "70대",
            pairedUserId = DemoLoginService.DEMO_CHILD_ID,
        )
        val service = DemoLoginService(StubUserRepository(mapOf(parent.id to parent)), jwtTokenProvider)

        val explicit = service.handle(DemoLoginCommand.In(role = Role.PARENT, variant = DemoVariant.EVAL))
        val implicit = service.handle(DemoLoginCommand.In(role = Role.PARENT))

        assertEquals(explicit.userId, implicit.userId)
    }

    @Test
    fun `demo login for the DEMO variant resolves to a different account than EVAL`() {
        val evalParent = User(
            id = DemoLoginService.DEMO_PARENT_ID,
            role = Role.PARENT,
            name = "데모 부모",
            ageGroup = "70대",
            pairedUserId = DemoLoginService.DEMO_CHILD_ID,
        )
        val showParent = User(
            id = DemoLoginService.SHOW_PARENT_ID,
            role = Role.PARENT,
            name = "시연 부모",
            ageGroup = "70대",
            pairedUserId = DemoLoginService.SHOW_CHILD_ID,
        )
        val service = DemoLoginService(
            StubUserRepository(mapOf(evalParent.id to evalParent, showParent.id to showParent)),
            jwtTokenProvider,
        )

        val evalOut = service.handle(DemoLoginCommand.In(role = Role.PARENT, variant = DemoVariant.EVAL))
        val demoOut = service.handle(DemoLoginCommand.In(role = Role.PARENT, variant = DemoVariant.DEMO))

        assertEquals(DemoLoginService.DEMO_PARENT_ID, evalOut.userId)
        assertEquals(DemoLoginService.SHOW_PARENT_ID, demoOut.userId)
        assertNotEquals(evalOut.userId, demoOut.userId)
    }

    @Test
    fun `demo login throws when the seed migration has not been applied`() {
        val service = DemoLoginService(StubUserRepository(emptyMap()), jwtTokenProvider)

        assertThrows<EntityNotFoundException> {
            service.handle(DemoLoginCommand.In(role = Role.CHILD))
        }
    }

    private class StubUserRepository(private val users: Map<UUID, User>) : UserRepositoryPort {
        override fun save(user: User): User = user
        override fun findById(id: UUID): User? = users[id]
        override fun findByInviteCode(inviteCode: String): User? = null
        override fun existsByInviteCode(inviteCode: String): Boolean = false
        override fun findByGoogleId(googleId: String): User? = null
    }
}
