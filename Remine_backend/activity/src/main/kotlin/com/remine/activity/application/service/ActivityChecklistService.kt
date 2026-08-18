package com.remine.activity.application.service

import com.remine.activity.application.port.inbound.GetChecklistQuery
import com.remine.activity.application.port.inbound.GetCheerMessageSuggestionsQuery
import com.remine.activity.application.port.inbound.SendCheerCommand
import com.remine.activity.application.port.inbound.ToggleChecklistItemCommand
import com.remine.activity.application.port.outbound.ActivityCheerRepositoryPort
import com.remine.activity.application.port.outbound.ActivityChecklistItemRepositoryPort
import com.remine.activity.application.port.outbound.CheerMessageGeneratorPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.ActivityCheer
import com.remine.activity.domain.ActivityChecklistItem
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Service
@Transactional
class ActivityChecklistService(
    private val checklistItemRepository: ActivityChecklistItemRepositoryPort,
    private val cheerRepository: ActivityCheerRepositoryPort,
    private val dailyActivityStatRepository: DailyActivityStatRepositoryPort,
    private val cheerMessageGenerator: CheerMessageGeneratorPort,
) : ToggleChecklistItemCommand,
    SendCheerCommand,
    GetChecklistQuery,
    GetCheerMessageSuggestionsQuery {

    override fun handle(command: ToggleChecklistItemCommand.In): ToggleChecklistItemCommand.Out {
        val item = checklistItemRepository.findById(command.checklistItemId)
            ?: throw EntityNotFoundException("Checklist item not found: ${command.checklistItemId}")
        requireOwnPair(item, command.requestedByParentUserId)

        val updated = item.copy(
            done = command.done,
            completedAt = if (command.done) Instant.now() else null,
            updatedAt = Instant.now(),
        )
        val saved = checklistItemRepository.save(updated)
        return ToggleChecklistItemCommand.Out(entity = saved)
    }

    override fun handle(command: SendCheerCommand.In): SendCheerCommand.Out {
        val item = checklistItemRepository.findById(command.checklistItemId)
            ?: throw EntityNotFoundException("Checklist item not found: ${command.checklistItemId}")
        requireOwnPair(item, command.requestedByParentUserId)

        val today = LocalDate.now()
        val existingCheers = cheerRepository.findByChecklistItemIdAndSenderUserId(item.id, command.senderUserId)
        val alreadySentToday = existingCheers.any { cheer ->
            val cheerDate = cheer.sentAt.atZone(ZoneId.systemDefault()).toLocalDate()
            cheerDate == today
        }
        if (alreadySentToday) {
            return SendCheerCommand.Out(entity = null)
        }

        val cheer = ActivityCheer(
            checklistItemId = item.id,
            senderUserId = command.senderUserId,
            sentAt = Instant.now(),
        )
        val saved = cheerRepository.save(cheer)
        return SendCheerCommand.Out(entity = saved)
    }

    override fun handle(query: GetChecklistQuery.In): GetChecklistQuery.Out {
        val existing = checklistItemRepository.findByUserIdAndStatDate(query.userId, query.statDate)
        if (existing.isNotEmpty()) {
            return GetChecklistQuery.Out(items = existing)
        }

        val defaultTypes = listOf("SLEEP", "BREAKFAST", "WALK", "QUIZ")
        val itemsToCreate = defaultTypes.map { type ->
            ActivityChecklistItem(
                userId = query.userId,
                statDate = query.statDate,
                type = type,
                done = false,
                completedAt = null,
                note = null,
            )
        }
        val saved = checklistItemRepository.saveAll(itemsToCreate)
        return GetChecklistQuery.Out(items = saved)
    }

    override fun handle(query: GetCheerMessageSuggestionsQuery.In): GetCheerMessageSuggestionsQuery.Out {
        val item = checklistItemRepository.findById(query.checklistItemId)
            ?: throw EntityNotFoundException("Checklist item not found: ${query.checklistItemId}")
        requireOwnPair(item, query.requestedByParentUserId)

        val stat = dailyActivityStatRepository.findByUserIdAndStatDate(item.userId, item.statDate)

        fun calcPercent(value: Int, goal: Int): Int = if (goal > 0) minOf(100, (value * 100) / goal) else 0

        val suggestions = cheerMessageGenerator.generateSuggestions(
            itemType = item.type,
            stat = stat,
            sleepPercent = stat?.let { calcPercent(it.sleepMinutes, it.sleepGoalMinutes) } ?: 0,
            stepsPercent = stat?.let { calcPercent(it.steps, it.stepsGoal) } ?: 0,
            outingPercent = stat?.let { calcPercent(it.outingCount, it.outingGoal) } ?: 0,
            socialPercent = stat?.let { calcPercent(it.socialContactCount, it.socialGoal) } ?: 0,
        )
        return GetCheerMessageSuggestionsQuery.Out(suggestions = suggestions)
    }

    private fun requireOwnPair(item: ActivityChecklistItem, requestedByParentUserId: UUID) {
        if (item.userId != requestedByParentUserId) {
            throw ForbiddenException("Checklist item ${item.id} does not belong to your family")
        }
    }
}
