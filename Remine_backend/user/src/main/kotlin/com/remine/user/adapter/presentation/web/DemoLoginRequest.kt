package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.Role
import com.remine.user.domain.DemoVariant
import javax.validation.constraints.NotNull

data class DemoLoginRequest(
    @field:NotNull
    val role: Role,
    // Defaults to EVAL so any existing caller/URL keeps behaving exactly as before —
    // the EVAL account must never change behavior from adding this field.
    val variant: DemoVariant = DemoVariant.EVAL,
)
