package com.remine.common.web

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.common.domain.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(ex: EntityNotFoundException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("NOT_FOUND", ex.message ?: "Not found"))

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(ex: InvalidRequestException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("INVALID_REQUEST", ex.message ?: "Invalid request"))

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(ex: UnauthorizedException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("UNAUTHORIZED", ex.message ?: "Unauthorized"))

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unhandled exception", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail("INTERNAL_ERROR", "Internal server error"))
    }
}
