package com.remine.user.adapter.presentation.web

import com.remine.common.web.ApiResponse
import com.remine.user.application.port.inbound.DemoLoginCommand
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

/**
 * Demo-only entry point. It deliberately accepts no credentials: its sole purpose is to give an AI
 * product reviewer and live demo presenters a zero-friction way into a fixed, pre-seeded pair of
 * demo accounts. It is not a general authentication mechanism.
 */
// Served from the `user` module rather than `auth` because `auth` cannot depend on `user`
// (dependency direction is adapter -> application -> domain, and `user` already depends on `auth`).
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val demoLoginCommand: DemoLoginCommand,
) {

    @PostMapping("/demo-login")
    fun demoLogin(
        @Valid @RequestBody request: DemoLoginRequest,
    ): ApiResponse<DemoLoginResponse> {
        val out = demoLoginCommand.handle(DemoLoginCommand.In(role = request.role, variant = request.variant))
        return ApiResponse.ok(
            DemoLoginResponse(
                userId = out.userId,
                role = out.role,
                name = out.name,
                accessToken = out.accessToken,
                pairedUserId = out.pairedUserId,
            ),
        )
    }
}
