package com.remine.app.composition

import com.remine.common.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Demo-only utility, no credential required — mirrors demo-login's own "no credential" design
 * (see AuthController). It can only ever touch the DEMO variant's fixed account pair (see
 * DemoResetService), never the EVAL pair or any real user, so it's safe to leave unauthenticated.
 */
@RestController
@RequestMapping("/api/v1/admin/demo")
class DemoResetController(
    private val demoResetService: DemoResetService,
) {

    @PostMapping("/reset")
    fun reset(): ApiResponse<Unit> {
        demoResetService.reset()
        return ApiResponse.ok(Unit)
    }
}
