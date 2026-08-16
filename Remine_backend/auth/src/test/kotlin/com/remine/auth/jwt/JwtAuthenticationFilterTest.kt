package com.remine.auth.jwt

import com.remine.auth.domain.Role
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class JwtAuthenticationFilterTest {

    private val jwtTokenProvider = JwtTokenProvider(
        "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256",
        604800000,
    )
    private val filter = JwtAuthenticationFilter(jwtTokenProvider)

    private class RecordingFilterChain : FilterChain {
        var invoked = false
            private set

        override fun doFilter(request: ServletRequest, response: ServletResponse) {
            invoked = true
        }
    }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `missing Authorization header leaves the security context empty but continues the chain`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = RecordingFilterChain()

        filter.doFilter(request, response, chain)

        assertTrue(chain.invoked)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `invalid token leaves the security context empty but continues the chain`() {
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer not-a-real-jwt")
        val response = MockHttpServletResponse()
        val chain = RecordingFilterChain()

        filter.doFilter(request, response, chain)

        assertTrue(chain.invoked)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `valid token authenticates the request with the matching role authority`() {
        val userId = UUID.randomUUID()
        val token = jwtTokenProvider.generateToken(userId, Role.PARENT, null)
        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer $token")
        val response = MockHttpServletResponse()
        val chain = RecordingFilterChain()

        filter.doFilter(request, response, chain)

        assertTrue(chain.invoked)
        val authentication = SecurityContextHolder.getContext().authentication
        assertTrue(authentication != null && authentication.isAuthenticated)
        val principal = authentication!!.principal as com.remine.auth.domain.RemineUserPrincipal
        assertEquals(userId, principal.userId)
        assertTrue(authentication.authorities.any { it.authority == "ROLE_PARENT" })
    }
}
