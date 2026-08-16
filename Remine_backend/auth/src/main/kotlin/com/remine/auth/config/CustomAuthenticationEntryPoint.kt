package com.remine.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.remine.common.web.ApiResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"
        val body = ApiResponse.fail("UNAUTHORIZED", "Authentication is required")
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}
