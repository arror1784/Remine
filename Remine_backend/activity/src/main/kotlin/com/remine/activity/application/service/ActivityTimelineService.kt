package com.remine.activity.application.service

import com.remine.activity.application.port.inbound.GetTimelineQuery
import com.remine.activity.application.port.inbound.RecordTimelineEventCommand
import com.remine.activity.application.port.outbound.ActivityTimelineEventRepositoryPort
import com.remine.activity.domain.ActivityTimelineEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ActivityTimelineService(
    private val timelineEventRepository: ActivityTimelineEventRepositoryPort,
) : RecordTimelineEventCommand,
    GetTimelineQuery {

    override fun handle(command: RecordTimelineEventCommand.In): RecordTimelineEventCommand.Out {
        val event = ActivityTimelineEvent(
            userId = command.userId,
            statDate = command.statDate,
            occurredAt = command.occurredAt,
            label = command.label,
            colorHint = command.colorHint,
        )
        val saved = timelineEventRepository.save(event)
        return RecordTimelineEventCommand.Out(entity = saved)
    }

    @Transactional(readOnly = true)
    override fun handle(query: GetTimelineQuery.In): GetTimelineQuery.Out {
        val events = timelineEventRepository.findByUserIdAndStatDateOrderByOccurredAtAsc(
            query.userId,
            query.statDate,
        )
        return GetTimelineQuery.Out(events = events)
    }
}
