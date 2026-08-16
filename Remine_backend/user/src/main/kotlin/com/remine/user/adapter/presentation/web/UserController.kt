package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.common.web.ApiResponse
import com.remine.user.application.port.inbound.GetMyProfileQuery
import com.remine.user.application.port.inbound.GetPairedUserQuery
import com.remine.user.application.port.inbound.PairWithInviteCodeCommand
import com.remine.user.application.port.inbound.SignUpCommand
import com.remine.user.application.port.inbound.UpdateProfileCommand
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val signUpCommand: SignUpCommand,
    private val updateProfileCommand: UpdateProfileCommand,
    private val pairWithInviteCodeCommand: PairWithInviteCodeCommand,
    private val getMyProfileQuery: GetMyProfileQuery,
    private val getPairedUserQuery: GetPairedUserQuery,
) {

    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ApiResponse<SignUpResponse> {
        val out = signUpCommand.handle(
            SignUpCommand.In(
                role = request.role,
                name = request.name,
                ageGroup = request.ageGroup,
                interests = request.interests,
                email = request.email,
                googleId = request.googleId,
            ),
        )
        return ApiResponse.ok(
            SignUpResponse(
                userId = out.userId,
                inviteCode = out.inviteCode,
                accessToken = out.accessToken,
            ),
        )
    }

    @PatchMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ApiResponse<UserResponse> {
        val out = updateProfileCommand.handle(
            UpdateProfileCommand.In(
                userId = principal.userId,
                name = request.name,
                ageGroup = request.ageGroup,
                interests = request.interests,
            ),
        )
        return ApiResponse.ok(UserResponse.from(out.entity))
    }

    @PreAuthorize("hasRole('CHILD')")
    @PostMapping("/me/pairing")
    fun pairWithInviteCode(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: PairingRequest,
    ): ApiResponse<PairingResponse> {
        val out = pairWithInviteCodeCommand.handle(
            PairWithInviteCodeCommand.In(
                childUserId = principal.userId,
                inviteCode = request.inviteCode,
            ),
        )
        return ApiResponse.ok(
            PairingResponse(
                parentUserId = out.parentUserId,
                accessToken = out.accessToken,
            ),
        )
    }

    @GetMapping("/me")
    fun getMyProfile(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<UserResponse> {
        val out = getMyProfileQuery.handle(
            GetMyProfileQuery.In(userId = principal.userId),
        )
        return ApiResponse.ok(UserResponse.from(out.entity))
    }

    @GetMapping("/me/paired")
    fun getPairedUser(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<UserResponse?> {
        val out = getPairedUserQuery.handle(
            GetPairedUserQuery.In(userId = principal.userId),
        )
        val response = out.pairedEntity?.let { UserResponse.from(it) }
        return ApiResponse.ok(response)
    }
}
