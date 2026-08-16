package com.remine.user.application.service

import com.remine.auth.domain.Role
import com.remine.auth.jwt.JwtTokenProvider
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.user.application.port.inbound.GetMyProfileQuery
import com.remine.user.application.port.inbound.GetPairedUserQuery
import com.remine.user.application.port.inbound.PairWithInviteCodeCommand
import com.remine.user.application.port.inbound.SignUpCommand
import com.remine.user.application.port.inbound.UpdateProfileCommand
import com.remine.user.application.port.outbound.UserRepositoryPort
import com.remine.user.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class UserServiceTest {

    private val fakeUserRepository = FakeUserRepository()
    private val jwtTokenProvider = JwtTokenProvider("test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256", 604800000)

    private lateinit var signUpService: SignUpService
    private lateinit var updateProfileService: UpdateProfileService
    private lateinit var pairWithInviteCodeService: PairWithInviteCodeService
    private lateinit var getMyProfileService: GetMyProfileService
    private lateinit var getPairedUserService: GetPairedUserService

    @BeforeEach
    fun setUp() {
        fakeUserRepository.clear()
        signUpService = SignUpService(fakeUserRepository, jwtTokenProvider)
        updateProfileService = UpdateProfileService(fakeUserRepository)
        pairWithInviteCodeService = PairWithInviteCodeService(fakeUserRepository, jwtTokenProvider)
        getMyProfileService = GetMyProfileService(fakeUserRepository)
        getPairedUserService = GetPairedUserService(fakeUserRepository)
    }

    @Test
    fun `parent signup generates invite code and token`() {
        val out = signUpService.handle(
            SignUpCommand.In(
                role = Role.PARENT,
                name = "부모님",
                ageGroup = "60대",
                interests = listOf("산책", "원예"),
            ),
        )

        assertNotNull(out.userId)
        assertNotNull(out.inviteCode)
        assertTrue(out.inviteCode!!.startsWith("REMIND-"))
        assertNotNull(out.accessToken)

        val user = fakeUserRepository.findById(out.userId)
        assertNotNull(user)
        assertEquals(Role.PARENT, user!!.role)
        assertEquals("부모님", user.name)
        assertEquals("60대", user.ageGroup)
        assertEquals(listOf("산책", "원예"), user.interests)
    }

    @Test
    fun `child signup does not have invite code`() {
        val out = signUpService.handle(
            SignUpCommand.In(
                role = Role.CHILD,
                name = "자녀",
                ageGroup = "20대",
                interests = listOf("무시되는관심사"),
            ),
        )

        assertNotNull(out.userId)
        assertNull(out.inviteCode)
        assertNotNull(out.accessToken)

        val user = fakeUserRepository.findById(out.userId)
        assertNotNull(user)
        assertEquals(Role.CHILD, user!!.role)
        assertTrue(user.interests.isEmpty())
    }

    @Test
    fun `signup throws for invalid age group`() {
        assertThrows<InvalidRequestException> {
            signUpService.handle(
                SignUpCommand.In(
                    role = Role.CHILD,
                    name = "자녀",
                    ageGroup = "100대",
                ),
            )
        }
    }

    @Test
    fun `update profile updates non-null fields`() {
        val parentOut = signUpService.handle(
            SignUpCommand.In(
                role = Role.PARENT,
                name = "부모",
                ageGroup = "60대",
                interests = listOf("등산"),
            ),
        )

        val updated = updateProfileService.handle(
            UpdateProfileCommand.In(
                userId = parentOut.userId,
                name = "새부모",
                ageGroup = "70대",
                interests = listOf("여행", "독서"),
            ),
        )

        assertEquals("새부모", updated.entity.name)
        assertEquals("70대", updated.entity.ageGroup)
        assertEquals(listOf("여행", "독서"), updated.entity.interests)
    }

    @Test
    fun `pair with invite code links parent and child`() {
        val parentOut = signUpService.handle(
            SignUpCommand.In(
                role = Role.PARENT,
                name = "부모",
                ageGroup = "60대",
            ),
        )
        val childOut = signUpService.handle(
            SignUpCommand.In(
                role = Role.CHILD,
                name = "자녀",
                ageGroup = "30대",
            ),
        )

        val pairOut = pairWithInviteCodeService.handle(
            PairWithInviteCodeCommand.In(
                childUserId = childOut.userId,
                inviteCode = parentOut.inviteCode!!,
            ),
        )

        assertEquals(parentOut.userId, pairOut.parentUserId)
        assertNotNull(pairOut.accessToken)

        val parent = fakeUserRepository.findById(parentOut.userId)!!
        val child = fakeUserRepository.findById(childOut.userId)!!

        assertEquals(child.id, parent.pairedUserId)
        assertEquals(parent.id, child.pairedUserId)
    }

    @Test
    fun `pair with invalid invite code throws EntityNotFoundException`() {
        val childOut = signUpService.handle(
            SignUpCommand.In(
                role = Role.CHILD,
                name = "자녀",
                ageGroup = "30대",
            ),
        )

        assertThrows<EntityNotFoundException> {
            pairWithInviteCodeService.handle(
                PairWithInviteCodeCommand.In(
                    childUserId = childOut.userId,
                    inviteCode = "REMIND-INVALID",
                ),
            )
        }
    }

    @Test
    fun `getMyProfile and getPairedUser queries work correctly`() {
        val parentOut = signUpService.handle(
            SignUpCommand.In(
                role = Role.PARENT,
                name = "부모",
                ageGroup = "60대",
            ),
        )
        val childOut = signUpService.handle(
            SignUpCommand.In(
                role = Role.CHILD,
                name = "자녀",
                ageGroup = "30대",
            ),
        )

        val myProfile = getMyProfileService.handle(GetMyProfileQuery.In(userId = parentOut.userId))
        assertEquals(parentOut.userId, myProfile.entity.id)

        val unpaired = getPairedUserService.handle(GetPairedUserQuery.In(userId = parentOut.userId))
        assertNull(unpaired.pairedEntity)

        pairWithInviteCodeService.handle(
            PairWithInviteCodeCommand.In(
                childUserId = childOut.userId,
                inviteCode = parentOut.inviteCode!!,
            ),
        )

        val paired = getPairedUserService.handle(GetPairedUserQuery.In(userId = parentOut.userId))
        assertNotNull(paired.pairedEntity)
        assertEquals(childOut.userId, paired.pairedEntity!!.id)
    }

    private class FakeUserRepository : UserRepositoryPort {
        private val users = ConcurrentHashMap<UUID, User>()

        fun clear() {
            users.clear()
        }

        override fun save(user: User): User {
            users[user.id] = user
            return user
        }

        override fun findById(id: UUID): User? = users[id]

        override fun findByInviteCode(inviteCode: String): User? {
            return users.values.firstOrNull { it.inviteCode == inviteCode }
        }

        override fun existsByInviteCode(inviteCode: String): Boolean {
            return users.values.any { it.inviteCode == inviteCode }
        }

        override fun findByGoogleId(googleId: String): User? {
            return users.values.firstOrNull { it.googleId == googleId }
        }
    }
}
