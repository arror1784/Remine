package com.remine.app.composition

import com.remine.activity.application.port.outbound.ActivityChecklistItemRepositoryPort
import com.remine.activity.application.port.outbound.ActivityTimelineEventRepositoryPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.ActivityChecklistItem
import com.remine.activity.domain.ActivityTimelineEvent
import com.remine.activity.domain.DailyActivityStat
import com.remine.family.application.port.outbound.FamilyPostRepositoryPort
import com.remine.family.domain.FamilyPost
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizAttemptRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizAttempt
import com.remine.memory.domain.MemoryQuizDraftQuestion
import com.remine.memory.domain.MemoryQuizQuestion
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.domain.ChatMessage
import com.remine.user.application.service.DemoLoginService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class DemoResetServiceTest {

    private val parentId = DemoLoginService.SHOW_PARENT_ID
    private val childId = DemoLoginService.SHOW_CHILD_ID
    private val evalParentId = DemoLoginService.DEMO_PARENT_ID

    @Test
    fun `reset wipes only the DEMO account's checklist, stats, and timeline, then reseeds today's baseline`() {
        val checklistRepo = InMemoryChecklistItemRepository()
        val statRepo = InMemoryDailyActivityStatRepository()
        val timelineRepo = InMemoryTimelineEventRepository()

        // Stale junk for the DEMO account, plus an untouched row for the EVAL account.
        checklistRepo.saveAll(
            listOf(
                ActivityChecklistItem(userId = parentId, statDate = LocalDate.now().minusDays(5), type = "WALK", done = true),
                ActivityChecklistItem(userId = evalParentId, statDate = LocalDate.now(), type = "WALK", done = true),
            ),
        )
        statRepo.save(DailyActivityStat(userId = parentId, statDate = LocalDate.now().minusDays(5)))
        statRepo.save(DailyActivityStat(userId = evalParentId, statDate = LocalDate.now()))
        timelineRepo.save(
            ActivityTimelineEvent(
                userId = parentId,
                statDate = LocalDate.now().minusDays(5),
                occurredAt = Instant.now(),
                label = "stale",
            ),
        )

        val service = DemoResetService(
            checklistItemRepository = checklistRepo,
            dailyActivityStatRepository = statRepo,
            timelineEventRepository = timelineRepo,
            memoryPhotoRepository = InMemoryMemoryPhotoRepository(),
            memoryQuizQuestionRepository = InMemoryMemoryQuizQuestionRepository(),
            memoryQuizDraftQuestionRepository = InMemoryMemoryQuizDraftQuestionRepository(),
            memoryQuizAttemptRepository = InMemoryMemoryQuizAttemptRepository(),
            familyPostRepository = InMemoryFamilyPostRepository(),
            chatMessageRepository = InMemoryChatMessageRepository(),
        )

        service.reset()

        val demoChecklist = checklistRepo.store.values.filter { it.userId == parentId }
        assertEquals(4, demoChecklist.size)
        assertTrue(demoChecklist.all { it.statDate == LocalDate.now() })
        assertEquals(setOf("SLEEP", "BREAKFAST", "WALK", "QUIZ"), demoChecklist.map { it.type }.toSet())
        assertEquals(2, demoChecklist.count { it.done })

        assertEquals(1, statRepo.store.values.count { it.userId == parentId })
        assertEquals(LocalDate.now(), statRepo.store.values.first { it.userId == parentId }.statDate)

        assertTrue(timelineRepo.store.values.none { it.userId == parentId && it.label == "stale" })
        assertEquals(2, timelineRepo.store.values.count { it.userId == parentId })

        // EVAL account must be completely untouched.
        assertEquals(1, checklistRepo.store.values.count { it.userId == evalParentId })
        assertEquals(1, statRepo.store.values.count { it.userId == evalParentId })
    }

    @Test
    fun `reset wipes memory photos, quiz questions, and quiz attempts for both DEMO accounts`() {
        val photoRepo = InMemoryMemoryPhotoRepository()
        val questionRepo = InMemoryMemoryQuizQuestionRepository()
        val draftQuestionRepo = InMemoryMemoryQuizDraftQuestionRepository()
        val attemptRepo = InMemoryMemoryQuizAttemptRepository()

        val photo = MemoryPhoto(
            ownerUserId = parentId,
            uploadedByUserId = childId,
            title = "stale photo",
            photoUrl = "https://example.com/x.jpg",
            memoryLabel = "2020년",
        )
        photoRepo.save(photo)
        questionRepo.saveAll(
            listOf(
                MemoryQuizQuestion(
                    memoryPhotoId = photo.id,
                    question = "q",
                    options = listOf("a", "b", "c", "d"),
                    correctOptionIndex = 0,
                    sortOrder = 0,
                ),
            ),
        )
        draftQuestionRepo.saveAll(listOf(MemoryQuizDraftQuestion(memoryPhotoId = photo.id, question = "q", sortOrder = 0)))
        attemptRepo.save(MemoryQuizAttempt(memoryPhotoId = photo.id, respondentUserId = childId, correctCount = 1, totalCount = 3))

        val evalPhoto = MemoryPhoto(
            ownerUserId = evalParentId,
            uploadedByUserId = evalParentId,
            title = "eval photo",
            photoUrl = "https://example.com/eval.jpg",
            memoryLabel = "2019년",
        )
        photoRepo.save(evalPhoto)

        val service = DemoResetService(
            checklistItemRepository = InMemoryChecklistItemRepository(),
            dailyActivityStatRepository = InMemoryDailyActivityStatRepository(),
            timelineEventRepository = InMemoryTimelineEventRepository(),
            memoryPhotoRepository = photoRepo,
            memoryQuizQuestionRepository = questionRepo,
            memoryQuizDraftQuestionRepository = draftQuestionRepo,
            memoryQuizAttemptRepository = attemptRepo,
            familyPostRepository = InMemoryFamilyPostRepository(),
            chatMessageRepository = InMemoryChatMessageRepository(),
        )

        service.reset()

        assertTrue(photoRepo.findAllByOwnerUserIdOrderByCreatedAtDesc(parentId).isEmpty())
        assertTrue(questionRepo.findAllByMemoryPhotoIdOrderBySortOrderAsc(photo.id).isEmpty())
        assertTrue(draftQuestionRepo.findAllByMemoryPhotoIdOrderBySortOrderAsc(photo.id).isEmpty())
        assertTrue(attemptRepo.findAttemptedPhotoIds(setOf(photo.id)).isEmpty())

        // EVAL account's photo must survive.
        assertEquals(1, photoRepo.findAllByOwnerUserIdOrderByCreatedAtDesc(evalParentId).size)
    }

    @Test
    fun `reset wipes family posts and chat messages for both DEMO accounts, leaving EVAL untouched`() {
        val postRepo = InMemoryFamilyPostRepository()
        val messageRepo = InMemoryChatMessageRepository()

        postRepo.save(FamilyPost(authorUserId = parentId, body = "stale post"))
        postRepo.save(FamilyPost(authorUserId = childId, body = "stale reply post"))
        postRepo.save(FamilyPost(authorUserId = evalParentId, body = "eval post"))
        messageRepo.save(ChatMessage(senderId = parentId, recipientId = childId, body = "hi"))
        messageRepo.save(ChatMessage(senderId = evalParentId, recipientId = DemoLoginService.DEMO_CHILD_ID, body = "eval hi"))

        val service = DemoResetService(
            checklistItemRepository = InMemoryChecklistItemRepository(),
            dailyActivityStatRepository = InMemoryDailyActivityStatRepository(),
            timelineEventRepository = InMemoryTimelineEventRepository(),
            memoryPhotoRepository = InMemoryMemoryPhotoRepository(),
            memoryQuizQuestionRepository = InMemoryMemoryQuizQuestionRepository(),
            memoryQuizDraftQuestionRepository = InMemoryMemoryQuizDraftQuestionRepository(),
            memoryQuizAttemptRepository = InMemoryMemoryQuizAttemptRepository(),
            familyPostRepository = postRepo,
            chatMessageRepository = messageRepo,
        )

        service.reset()

        assertTrue(postRepo.findFeed(setOf(parentId, childId), null, 50).isEmpty())
        assertEquals(1, postRepo.findFeed(setOf(evalParentId), null, 50).size)
        assertEquals(0, messageRepo.countByPair(parentId, childId))
        assertEquals(1, messageRepo.countByPair(evalParentId, DemoLoginService.DEMO_CHILD_ID))
    }

    private class InMemoryChecklistItemRepository : ActivityChecklistItemRepositoryPort {
        val store = mutableMapOf<UUID, ActivityChecklistItem>()
        override fun findById(id: UUID): ActivityChecklistItem? = store[id]
        override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): List<ActivityChecklistItem> =
            store.values.filter { it.userId == userId && it.statDate == statDate }
        override fun save(item: ActivityChecklistItem): ActivityChecklistItem {
            store[item.id] = item
            return item
        }
        override fun saveAll(items: Collection<ActivityChecklistItem>): List<ActivityChecklistItem> {
            items.forEach { store[it.id] = it }
            return items.toList()
        }
        override fun deleteAllByUserId(userId: UUID) {
            store.values.removeIf { it.userId == userId }
        }
    }

    private class InMemoryDailyActivityStatRepository : DailyActivityStatRepositoryPort {
        val store = mutableMapOf<UUID, DailyActivityStat>()
        override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityStat? =
            store.values.firstOrNull { it.userId == userId && it.statDate == statDate }
        override fun findByUserIdAndStatDateIn(userId: UUID, statDates: Collection<LocalDate>): List<DailyActivityStat> =
            store.values.filter { it.userId == userId && it.statDate in statDates }
        override fun findByUserIdAndStatDateBetween(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<DailyActivityStat> =
            store.values.filter { it.userId == userId && !it.statDate.isBefore(startDate) && !it.statDate.isAfter(endDate) }
        override fun save(stat: DailyActivityStat): DailyActivityStat {
            store[stat.id] = stat
            return stat
        }
        override fun saveAll(stats: Collection<DailyActivityStat>): List<DailyActivityStat> {
            stats.forEach { store[it.id] = it }
            return stats.toList()
        }
        override fun deleteAllByUserId(userId: UUID) {
            store.values.removeIf { it.userId == userId }
        }
    }

    private class InMemoryTimelineEventRepository : ActivityTimelineEventRepositoryPort {
        val store = mutableMapOf<UUID, ActivityTimelineEvent>()
        override fun findByUserIdAndStatDateOrderByOccurredAtAsc(userId: UUID, statDate: LocalDate): List<ActivityTimelineEvent> =
            store.values.filter { it.userId == userId && it.statDate == statDate }.sortedBy { it.occurredAt }
        override fun save(event: ActivityTimelineEvent): ActivityTimelineEvent {
            store[event.id] = event
            return event
        }
        override fun deleteAllByUserId(userId: UUID) {
            store.values.removeIf { it.userId == userId }
        }
    }

    private class InMemoryMemoryPhotoRepository : MemoryPhotoRepositoryPort {
        val store = mutableMapOf<UUID, MemoryPhoto>()
        override fun save(photo: MemoryPhoto): MemoryPhoto {
            store[photo.id] = photo
            return photo
        }
        override fun findById(id: UUID): MemoryPhoto? = store[id]
        override fun findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId: UUID): List<MemoryPhoto> =
            store.values.filter { it.ownerUserId == ownerUserId }.sortedByDescending { it.createdAt }
        override fun countByOwnerUserId(ownerUserId: UUID): Int = store.values.count { it.ownerUserId == ownerUserId }
        override fun countByOwnerUserIdAndStatus(ownerUserId: UUID, status: MemoryPhotoStatus): Int =
            store.values.count { it.ownerUserId == ownerUserId && it.status == status }
        override fun countByOwnerUserIdAndCreatedAtGreaterThanEqual(ownerUserId: UUID, startOfMonth: Instant): Int =
            store.values.count { it.ownerUserId == ownerUserId && !it.createdAt.isBefore(startOfMonth) }
        override fun findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(ownerUserId: UUID, status: MemoryPhotoStatus): List<MemoryPhoto> =
            store.values.filter { it.ownerUserId == ownerUserId && it.status == status }.sortedByDescending { it.createdAt }
        override fun deleteAllByOwnerUserId(ownerUserId: UUID) {
            store.values.removeIf { it.ownerUserId == ownerUserId }
        }
    }

    private class InMemoryMemoryQuizQuestionRepository : MemoryQuizQuestionRepositoryPort {
        val store = mutableListOf<MemoryQuizQuestion>()
        override fun saveAll(questions: List<MemoryQuizQuestion>): List<MemoryQuizQuestion> {
            store.addAll(questions)
            return questions
        }
        override fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizQuestion> =
            store.filter { it.memoryPhotoId == memoryPhotoId }.sortedBy { it.sortOrder }
        override fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID) {
            store.removeIf { it.memoryPhotoId == memoryPhotoId }
        }
    }

    private class InMemoryMemoryQuizDraftQuestionRepository : MemoryQuizDraftQuestionRepositoryPort {
        val store = mutableListOf<MemoryQuizDraftQuestion>()
        override fun saveAll(questions: List<MemoryQuizDraftQuestion>): List<MemoryQuizDraftQuestion> {
            store.addAll(questions)
            return questions
        }
        override fun findAllByMemoryPhotoIdOrderBySortOrderAsc(memoryPhotoId: UUID): List<MemoryQuizDraftQuestion> =
            store.filter { it.memoryPhotoId == memoryPhotoId }.sortedBy { it.sortOrder }
        override fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID) {
            store.removeIf { it.memoryPhotoId == memoryPhotoId }
        }
    }

    private class InMemoryMemoryQuizAttemptRepository : MemoryQuizAttemptRepositoryPort {
        val store = mutableListOf<MemoryQuizAttempt>()
        override fun save(attempt: MemoryQuizAttempt): MemoryQuizAttempt {
            store.add(attempt)
            return attempt
        }
        override fun existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(memoryPhotoId: UUID, since: Instant): Boolean =
            store.any { it.memoryPhotoId == memoryPhotoId && !it.completedAt.isBefore(since) }
        override fun findAttemptedPhotoIds(memoryPhotoIds: Collection<UUID>): Set<UUID> =
            store.filter { it.memoryPhotoId in memoryPhotoIds }.map { it.memoryPhotoId }.toSet()
        override fun deleteAllByMemoryPhotoId(memoryPhotoId: UUID) {
            store.removeIf { it.memoryPhotoId == memoryPhotoId }
        }
    }

    private class InMemoryFamilyPostRepository : FamilyPostRepositoryPort {
        val store = mutableMapOf<UUID, FamilyPost>()
        override fun save(post: FamilyPost): FamilyPost {
            store[post.id] = post
            return post
        }
        override fun findById(id: UUID): FamilyPost? = store[id]
        override fun findFeed(pairUserIds: Set<UUID>, cursor: Instant?, limit: Int): List<FamilyPost> =
            store.values.filter { it.authorUserId in pairUserIds }.sortedByDescending { it.createdAt }.take(limit)
        override fun existsById(id: UUID): Boolean = store.containsKey(id)
        override fun deleteAllByAuthorUserId(authorUserId: UUID) {
            store.values.removeIf { it.authorUserId == authorUserId }
        }
    }

    private class InMemoryChatMessageRepository : ChatMessageRepositoryPort {
        val store = mutableListOf<ChatMessage>()
        override fun save(chatMessage: ChatMessage): ChatMessage {
            store.add(chatMessage)
            return chatMessage
        }
        override fun findThread(userAId: UUID, userBId: UUID, before: Instant?, limit: Int): List<ChatMessage> =
            store.filter { (it.senderId == userAId && it.recipientId == userBId) || (it.senderId == userBId && it.recipientId == userAId) }
        override fun countByPair(userAId: UUID, userBId: UUID): Int =
            store.count { (it.senderId == userAId && it.recipientId == userBId) || (it.senderId == userBId && it.recipientId == userAId) }
        override fun deleteAllByParticipant(userId: UUID) {
            store.removeIf { it.senderId == userId || it.recipientId == userId }
        }
    }
}
