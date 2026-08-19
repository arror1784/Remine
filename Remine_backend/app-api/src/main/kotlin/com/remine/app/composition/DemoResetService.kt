package com.remine.app.composition

import com.remine.activity.application.port.outbound.ActivityChecklistItemRepositoryPort
import com.remine.activity.application.port.outbound.ActivityTimelineEventRepositoryPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.ActivityChecklistItem
import com.remine.activity.domain.ActivityTimelineEvent
import com.remine.activity.domain.DailyActivityStat
import com.remine.family.application.port.outbound.FamilyPostRepositoryPort
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizAttemptRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.auth.domain.Role
import com.remine.user.application.service.DemoLoginService
import com.remine.user.domain.DemoVariant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

/**
 * Wipes and reseeds a "good-looking" baseline of business data for one demo-login variant's fixed
 * PARENT+CHILD pair (see DemoLoginService.userIdFor). Resetting EVAL never touches DEMO's data and
 * vice versa — the account IDs it operates on come only from DemoLoginService's hardcoded pairs,
 * never from caller input, so this can only ever affect one of the two known seed pairs and never
 * a real user.
 *
 * Lives in app-api rather than any single domain module because it needs outbound ports from
 * activity, memory, family, and message, which don't depend on each other.
 */
@Service
class DemoResetService(
    private val checklistItemRepository: ActivityChecklistItemRepositoryPort,
    private val dailyActivityStatRepository: DailyActivityStatRepositoryPort,
    private val timelineEventRepository: ActivityTimelineEventRepositoryPort,
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizQuestionRepository: MemoryQuizQuestionRepositoryPort,
    private val memoryQuizDraftQuestionRepository: MemoryQuizDraftQuestionRepositoryPort,
    private val memoryQuizAttemptRepository: MemoryQuizAttemptRepositoryPort,
    private val familyPostRepository: FamilyPostRepositoryPort,
    private val chatMessageRepository: ChatMessageRepositoryPort,
) {

    @Transactional
    fun reset(variant: DemoVariant) {
        val parentId = DemoLoginService.userIdFor(Role.PARENT, variant)
        val childId = DemoLoginService.userIdFor(Role.CHILD, variant)

        wipe(parentId, childId)
        reseedBaseline(parentId)
    }

    private fun wipe(parentId: UUID, childId: UUID) {
        checklistItemRepository.deleteAllByUserId(parentId)
        dailyActivityStatRepository.deleteAllByUserId(parentId)
        timelineEventRepository.deleteAllByUserId(parentId)

        // Photos can be owned by either account in this pair — wipe both sides.
        listOf(parentId, childId).forEach { ownerId ->
            val photos = memoryPhotoRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(ownerId)
            photos.forEach { photo ->
                memoryQuizQuestionRepository.deleteAllByMemoryPhotoId(photo.id)
                memoryQuizDraftQuestionRepository.deleteAllByMemoryPhotoId(photo.id)
                memoryQuizAttemptRepository.deleteAllByMemoryPhotoId(photo.id)
            }
            memoryPhotoRepository.deleteAllByOwnerUserId(ownerId)
        }

        familyPostRepository.deleteAllByAuthorUserId(parentId)
        familyPostRepository.deleteAllByAuthorUserId(childId)

        chatMessageRepository.deleteAllByParticipant(parentId)
        chatMessageRepository.deleteAllByParticipant(childId)
    }

    /**
     * Regenerated relative to LocalDate.now()/today's clock time on every call (not hardcoded
     * dates) so the demo always looks fresh, no matter how long after this code ships the reset
     * button gets pressed. Deliberately a believable partial-completion state, not 0% or 100%.
     * Memory photos, family posts, and messages are intentionally left empty after the wipe
     * above — those need real uploads / live interaction during the demo, not fabricated content.
     */
    private fun reseedBaseline(parentId: UUID) {
        val zone = ZoneId.of("Asia/Seoul")
        val today = LocalDate.now(zone)

        dailyActivityStatRepository.save(
            DailyActivityStat(
                userId = parentId,
                statDate = today,
                sleepMinutes = 390, // 6h30m of an 8h goal — good but not maxed out
                steps = 6200,
                outingCount = 1,
                socialContactCount = 1,
                sleepGoalMinutes = 480,
                stepsGoal = 8000,
                outingGoal = 1,
                socialGoal = 1,
            ),
        )

        val morningCompletedAt = today.atTime(LocalTime.of(8, 10)).atZone(zone).toInstant()
        checklistItemRepository.saveAll(
            listOf(
                ActivityChecklistItem(
                    userId = parentId,
                    statDate = today,
                    type = "SLEEP",
                    done = true,
                    completedAt = morningCompletedAt,
                ),
                ActivityChecklistItem(
                    userId = parentId,
                    statDate = today,
                    type = "BREAKFAST",
                    done = true,
                    completedAt = morningCompletedAt,
                ),
                ActivityChecklistItem(userId = parentId, statDate = today, type = "WALK", done = false),
                ActivityChecklistItem(userId = parentId, statDate = today, type = "QUIZ", done = false),
            ),
        )

        timelineEventRepository.save(
            ActivityTimelineEvent(
                userId = parentId,
                statDate = today,
                occurredAt = today.atTime(LocalTime.of(7, 0)).atZone(zone).toInstant(),
                label = "기상 및 수면 기록",
            ),
        )
        timelineEventRepository.save(
            ActivityTimelineEvent(
                userId = parentId,
                statDate = today,
                occurredAt = today.atTime(LocalTime.of(8, 10)).atZone(zone).toInstant(),
                label = "아침 식사 완료",
            ),
        )
    }
}
