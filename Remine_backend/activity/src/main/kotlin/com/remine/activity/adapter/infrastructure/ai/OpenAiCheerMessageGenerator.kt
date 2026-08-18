package com.remine.activity.adapter.infrastructure.ai

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.remine.activity.application.port.outbound.CheerMessageGeneratorPort
import com.remine.activity.domain.DailyActivityStat
import com.remine.client.openai.OpenAiClient
import com.remine.common.domain.exception.InvalidRequestException
import org.springframework.stereotype.Component

@Component
class OpenAiCheerMessageGenerator(
    private val openAiClient: OpenAiClient,
    private val objectMapper: ObjectMapper,
) : CheerMessageGeneratorPort {

    override fun generateSuggestions(
        itemType: String,
        stat: DailyActivityStat?,
        sleepPercent: Int,
        stepsPercent: Int,
        outingPercent: Int,
        socialPercent: Int,
    ): List<String> {
        val raw = openAiClient.completeJson(
            systemPrompt = suggestionsSystemPrompt(),
            userPrompt = suggestionsUserPrompt(itemType, stat, sleepPercent, stepsPercent, outingPercent, socialPercent),
        )
        return parseSuggestions(raw)
    }

    private fun parseSuggestions(raw: String): List<String> {
        val root = try {
            objectMapper.readTree(raw)
        } catch (e: JsonProcessingException) {
            throw InvalidRequestException("응원 메시지 생성에 실패했어요: 응답이 올바른 JSON이 아닙니다")
        }

        val suggestions = root.path("suggestions")
            .takeIf { it.isArray }
            ?.mapNotNull { node -> node.takeIf { it.isTextual }?.asText()?.trim()?.takeIf { it.isNotBlank() } }
            ?: emptyList()

        if (suggestions.isEmpty()) {
            throw InvalidRequestException("응원 메시지 생성에 실패했어요: 메시지가 비어 있습니다")
        }
        return suggestions.take(3)
    }

    private fun suggestionsSystemPrompt(): String = """
        당신은 자녀가 부모님께 보낼 짧은 응원/안부 메시지를 대신 만들어주는 도우미입니다.
        자녀가 지금 챙기려는 활동 항목과, 부모님의 오늘 활동 데이터(수면/걸음 수/외출/사회적 연락 달성률)를 참고해서,
        부모님께 자녀가 직접 보낼 수 있는 짧은 메시지 3개를 만들어주세요.

        [생성 규칙]
        1. 부모님(어르신) 본인에게 직접 이야기하는 2인칭 존댓말(따뜻하고 다정한 해요체)로 작성하세요.
        2. 각 메시지는 1문장, 20~40자 내외로 간결하게 작성하세요.
        3. 반드시 지금 챙기려는 활동 항목(아래 [챙기려는 항목] 참고)과 직접 관련된 내용이어야 합니다.
           예: WALK(산책)이면 산책을 권유/응원, SLEEP(수면)이면 잠을 잘 챙기시길 바라는 내용,
               BREAKFAST(아침식사)면 식사를 챙기시길 바라는 내용, QUIZ(추억 퀴즈)면 퀴즈를 풀어보시길 권유하는 내용.
        4. 오늘 활동 달성률이 낮은 항목이 있다면 자연스럽게 반영하되, 걱정하거나 다그치는 느낌은 피하세요.
        5. 3개는 서로 다른 결(안부를 묻는 질문형 / 다정하게 권유하는 형 / 응원하는 형)로 작성해서 다양하게 만들어주세요.

        응답은 반드시 아래 json 스키마를 따르는 단 하나의 JSON 객체로 작성하세요.
        {"suggestions": ["메시지1", "메시지2", "메시지3"]}
    """.trimIndent()

    private fun suggestionsUserPrompt(
        itemType: String,
        stat: DailyActivityStat?,
        sleepPercent: Int,
        stepsPercent: Int,
        outingPercent: Int,
        socialPercent: Int,
    ): String {
        val statSummary = if (stat == null) {
            "아직 오늘 활동 기록이 없습니다."
        } else {
            """
            - 수면: ${stat.sleepMinutes}분 / 목표 ${stat.sleepGoalMinutes}분 (달성률 ${sleepPercent}%)
            - 걸음 수: ${stat.steps}걸음 / 목표 ${stat.stepsGoal}걸음 (달성률 ${stepsPercent}%)
            - 외출: ${stat.outingCount}회 / 목표 ${stat.outingGoal}회 (달성률 ${outingPercent}%)
            - 사회적 연락: ${stat.socialContactCount}회 / 목표 ${stat.socialGoal}회 (달성률 ${socialPercent}%)
            """.trimIndent()
        }

        return """
            [챙기려는 항목]
            $itemType

            [오늘의 활동 요약]
            $statSummary

            위 정보를 참고해서 json 형식으로 응원 메시지 3개를 만들어주세요.
        """.trimIndent()
    }
}
