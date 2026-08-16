package com.remine.common.web

data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiError? = null,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(data = data)
        fun fail(code: String, message: String): ApiResponse<Nothing> =
            ApiResponse(error = ApiError(code = code, message = message))
    }
}
