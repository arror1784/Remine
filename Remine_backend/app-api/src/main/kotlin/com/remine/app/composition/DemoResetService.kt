package com.remine.app.composition

import com.remine.activity.application.port.outbound.ActivityChecklistItemRepositoryPort
import com.remine.activity.application.port.outbound.ActivityTimelineEventRepositoryPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.ActivityChecklistItem
import com.remine.activity.domain.ActivityTimelineEvent
import com.remine.activity.domain.DailyActivityStat
import com.remine.family.application.port.outbound.FamilyPostRepositoryPort
import com.remine.memory.application.port.outbound.ImageStoragePort
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizAttemptRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizQuestion
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.auth.domain.Role
import com.remine.user.application.service.DemoLoginService
import com.remine.user.domain.DemoVariant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.awt.Color
import java.awt.GradientPaint
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.imageio.ImageIO

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
    private val imageStoragePort: ImageStoragePort,
) {

    @Transactional
    fun reset(variant: DemoVariant) {
        val parentId = DemoLoginService.userIdFor(Role.PARENT, variant)
        val childId = DemoLoginService.userIdFor(Role.CHILD, variant)

        wipe(parentId, childId)
        reseedBaseline(parentId, childId)
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
     * Family posts and messages are intentionally left empty after the wipe above — those are
     * meant to be created live during the demo, not fabricated. Memory photos are the exception:
     * an empty gallery with no quiz to show was flagged as making the reset state look too bare,
     * so a handful of seed photos + ready-to-play quizzes are generated below.
     */
    private fun reseedBaseline(parentId: UUID, childId: UUID) {
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

        SEED_MEMORIES.forEach { memory ->
            val bytes = generateGradientJpeg(memory.topColor, memory.bottomColor)
            val photoUrl = imageStoragePort.store(bytes, "${memory.slug}.jpg", "image/jpeg")

            val photo = memoryPhotoRepository.save(
                MemoryPhoto(
                    ownerUserId = parentId,
                    uploadedByUserId = childId,
                    title = memory.title,
                    photoUrl = photoUrl,
                    memoryLabel = memory.memoryLabel,
                    status = MemoryPhotoStatus.QUIZ_ACTIVE,
                ),
            )

            memoryQuizQuestionRepository.saveAll(
                memory.questions.mapIndexed { index, q ->
                    MemoryQuizQuestion(
                        memoryPhotoId = photo.id,
                        question = q.question,
                        options = q.options,
                        correctOptionIndex = q.correctOptionIndex,
                        sortOrder = index,
                    )
                },
            )
        }
    }

    /**
     * A plain color-gradient JPEG, not a real photo — no external image source is fetched or
     * bundled (avoids both a network dependency during reset and any question of image rights).
     * `title`/`memoryLabel` are rendered as text by the frontend UI around the image, so the
     * image itself doesn't need any text baked in — just something visually pleasant to fill the
     * card. Pure Java2D (BufferedImage + Graphics2D), no AWT display/font access required.
     */
    private fun generateGradientJpeg(top: Color, bottom: Color): ByteArray {
        val width = 640
        val height = 480
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.paint = GradientPaint(0f, 0f, top, 0f, height.toFloat(), bottom)
        g.fillRect(0, 0, width, height)
        g.dispose()

        val out = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", out)
        return out.toByteArray()
    }

    private data class SeedQuestion(val question: String, val options: List<String>, val correctOptionIndex: Int)

    private data class SeedMemory(
        val slug: String,
        val title: String,
        val memoryLabel: String,
        val topColor: Color,
        val bottomColor: Color,
        val questions: List<SeedQuestion>,
    )

    private companion object {
        val SEED_MEMORIES = listOf(
            SeedMemory(
                slug = "picnic",
                title = "가족 소풍",
                memoryLabel = "1985년",
                topColor = Color(255, 214, 165),
                bottomColor = Color(255, 170, 165),
                questions = listOf(
                    SeedQuestion("이 사진은 몇 년도에 찍었을까요?", listOf("1985년", "1990년", "1978년", "2000년"), 0),
                    SeedQuestion("이날은 어떤 계절이었을까요?", listOf("봄", "여름", "가을", "겨울"), 1),
                    SeedQuestion("함께 있던 사람은 누구였을까요?", listOf("가족", "직장 동료", "친구", "이웃"), 0),
                ),
            ),
            SeedMemory(
                slug = "graduation",
                title = "졸업식",
                memoryLabel = "1978년",
                topColor = Color(165, 200, 255),
                bottomColor = Color(200, 165, 255),
                questions = listOf(
                    SeedQuestion("이 사진 속 행사는 무엇이었을까요?", listOf("졸업식", "결혼식", "생일잔치", "입학식"), 0),
                    SeedQuestion("이 추억은 몇 년도의 일이었을까요?", listOf("1978년", "1985년", "1995년", "2005년"), 0),
                    SeedQuestion("어떤 계절에 있었던 일일까요?", listOf("봄", "여름", "가을", "겨울"), 0),
                ),
            ),
            SeedMemory(
                slug = "birthday",
                title = "생신 잔치",
                memoryLabel = "2005년",
                topColor = Color(165, 255, 200),
                bottomColor = Color(255, 235, 165),
                questions = listOf(
                    SeedQuestion("이 사진 속 행사는 무엇이었을까요?", listOf("생신 잔치", "제사", "명절", "졸업식"), 0),
                    SeedQuestion("이 추억은 몇 년도의 일이었을까요?", listOf("2005년", "1995년", "1985년", "2015년"), 0),
                    SeedQuestion("함께 있던 사람은 누구였을까요?", listOf("가족", "친구", "이웃", "직장 동료"), 0),
                ),
            ),
        )
    }
}
