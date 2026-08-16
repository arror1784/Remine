package com.remine.call.application.service

import com.remine.call.application.port.inbound.GetCallStatsQuery
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset

@Service
@Transactional(readOnly = true)
class GetCallStatsService(
    private val callLogRepositoryPort: CallLogRepositoryPort,
) : GetCallStatsQuery {

    override fun handle(query: GetCallStatsQuery.In): GetCallStatsQuery.Out {
        val sinceInstant = query.sinceMonthStart.atStartOfDay(ZoneOffset.UTC).toInstant()
        val stats = callLogRepositoryPort.getCallStats(query.userId, sinceInstant)
        return GetCallStatsQuery.Out(
            count = stats.count,
            totalDurationSeconds = stats.totalDurationSeconds,
        )
    }
}
