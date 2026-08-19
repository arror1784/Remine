package com.remine.app.composition

import com.remine.common.web.ApiResponse
import com.remine.user.domain.DemoVariant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Demo-only utility, no credential required — mirrors demo-login's own "no credential" design
 * (see AuthController). `variant` selects which of the two known seed pairs (see
 * DemoLoginService) gets wiped/reseeded; it can never reach a real user's data.
 */
@RestController
@RequestMapping("/api/v1/admin/demo")
class DemoResetController(
    private val demoResetService: DemoResetService,
) {

    @PostMapping("/reset")
    fun reset(
        @RequestParam(defaultValue = "DEMO") variant: DemoVariant,
    ): ApiResponse<Unit> {
        demoResetService.reset(variant)
        return ApiResponse.ok(Unit)
    }
}
