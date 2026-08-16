package com.remine.auth.jwt

import com.remine.auth.domain.Role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtTokenProviderTest {

    private val provider = JwtTokenProvider(
        "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256",
        604800000,
    )

    @Test
    fun `generateToken then parse round-trips the claims for a paired child`() {
        val userId = UUID.randomUUID()
        val pairedUserId = UUID.randomUUID()

        val token = provider.generateToken(userId, Role.CHILD, pairedUserId)
        val principal = provider.parse(token)

        assertNotNull(principal)
        assertEquals(userId, principal!!.userId)
        assertEquals(Role.CHILD, principal.role)
        assertEquals(pairedUserId, principal.pairedUserId)
    }

    @Test
    fun `generateToken then parse round-trips an unpaired parent with null pairedUserId`() {
        val userId = UUID.randomUUID()

        val token = provider.generateToken(userId, Role.PARENT, null)
        val principal = provider.parse(token)

        assertNotNull(principal)
        assertEquals(userId, principal!!.userId)
        assertEquals(Role.PARENT, principal.role)
        assertNull(principal.pairedUserId)
    }

    @Test
    fun `parse returns null for a garbage token`() {
        assertNull(provider.parse("not-a-real-jwt"))
    }

    @Test
    fun `parse returns null for a token signed with a different secret`() {
        val otherProvider = JwtTokenProvider(
            "a-completely-different-secret-key-that-is-also-256-bits-long!!",
            604800000,
        )
        val token = otherProvider.generateToken(UUID.randomUUID(), Role.PARENT, null)

        assertNull(provider.parse(token))
    }

    @Test
    fun `parse returns null for an expired token`() {
        val expiredProvider = JwtTokenProvider(
            "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256",
            -1000,
        )
        val token = expiredProvider.generateToken(UUID.randomUUID(), Role.PARENT, null)

        assertNull(provider.parse(token))
    }
}
