package com.remine.common.web

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.common.domain.exception.UnauthorizedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `EntityNotFoundException maps to 404 with NOT_FOUND code`() {
        val response = handler.handleNotFound(EntityNotFoundException("no such thing"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("NOT_FOUND", response.body?.error?.code)
        assertEquals("no such thing", response.body?.error?.message)
    }

    @Test
    fun `InvalidRequestException maps to 400 with INVALID_REQUEST code`() {
        val response = handler.handleInvalidRequest(InvalidRequestException("bad input"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("INVALID_REQUEST", response.body?.error?.code)
        assertEquals("bad input", response.body?.error?.message)
    }

    @Test
    fun `UnauthorizedException maps to 401 with UNAUTHORIZED code`() {
        val response = handler.handleUnauthorized(UnauthorizedException("no token"))

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("UNAUTHORIZED", response.body?.error?.code)
        assertEquals("no token", response.body?.error?.message)
    }

    @Test
    fun `ForbiddenException maps to 403 with FORBIDDEN code`() {
        val response = handler.handleForbidden(ForbiddenException("not your resource"))

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("FORBIDDEN", response.body?.error?.code)
        assertEquals("not your resource", response.body?.error?.message)
    }

    @Test
    fun `AccessDeniedException maps to 403 with FORBIDDEN code`() {
        val response = handler.handleAccessDenied(AccessDeniedException("role check failed"))

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("FORBIDDEN", response.body?.error?.code)
        assertEquals("role check failed", response.body?.error?.message)
    }

    @Test
    fun `unexpected exceptions map to 500 with INTERNAL_ERROR code and a generic message`() {
        val response = handler.handleUnexpected(RuntimeException("db is on fire"))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("INTERNAL_ERROR", response.body?.error?.code)
        assertEquals("Internal server error", response.body?.error?.message)
    }
}
