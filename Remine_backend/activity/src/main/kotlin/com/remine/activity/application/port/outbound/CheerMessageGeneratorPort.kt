package com.remine.activity.application.port.outbound

import com.remine.activity.domain.DailyActivityStat

/** Outbound port to the AI cheer-message generator (see `OpenAiCheerMessageGenerator`). */
interface CheerMessageGeneratorPort {
    fun generateSuggestions(
        itemType: String,
        stat: DailyActivityStat?,
        sleepPercent: Int,
        stepsPercent: Int,
        outingPercent: Int,
        socialPercent: Int,
    ): List<String>
}
