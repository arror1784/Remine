package com.remine.activity.application.service

import com.remine.activity.application.port.inbound.GetChecklistQuery
import com.remine.activity.application.port.inbound.SendCheerCommand
import com.remine.activity.application.port.inbound.ToggleChecklistItemCommand
import com.remine.activity.application.port.outbound.ActivityCheerRepositoryPort
import com.remine.activity.application.port.outbound.ActivityChecklistItemRepositoryPort
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
) : ToggleChecklistItemCommand,
    SendCheerCommand,
    GetChecklistQuery {

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

    private fun requireOwnPair(item: ActivityChecklistItem, requestedByParentUserId: UUID) {
        if (item.userId != requestedByParentUserId) {
            throw ForbiddenException("Checklist item ${item.id} does not belong to your family")
        }
    }
}
