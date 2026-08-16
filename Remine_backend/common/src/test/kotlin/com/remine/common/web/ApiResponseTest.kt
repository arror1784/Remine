package com.remine.common.web

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ApiResponseTest {

    @Test
    fun `ok wraps data with no error`() {
        val response = ApiResponse.ok("payload")

        assertEquals("payload", response.data)
        assertNull(response.error)
    }

    @Test
    fun `fail wraps an error with no data`() {
        val response = ApiResponse.fail("NOT_FOUND", "missing")

        assertNull(response.data)
        assertEquals("NOT_FOUND", response.error?.code)
        assertEquals("missing", response.error?.message)
    }
}
