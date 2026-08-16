package com.remine.activity.adapter.infrastructure.ai

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.remine.activity.application.port.outbound.ActivityRecommendationGeneratorPort
import com.remine.activity.domain.DailyActivityRecommendationActionType
import com.remine.activity.domain.DailyActivityStat
import com.remine.client.openai.OpenAiClient
import com.remine.common.domain.exception.InvalidRequestException
import org.springframework.stereotype.Component

@Component
class OpenAiActivityRecommendationGenerator(
    private val openAiClient: OpenAiClient,
    private val objectMapper: ObjectMapper,
) : ActivityRecommendationGeneratorPort {

    override fun generateRecommendation(
        stat: DailyActivityStat,
        sleepPercent: Int,
        stepsPercent: Int,
        outingPercent: Int,
        socialPercent: Int,
    ): ActivityRecommendationGeneratorPort.GeneratedRecommendation {
        val raw = openAiClient.completeJson(
            systemPrompt = recommendationSystemPrompt(),
            userPrompt = recommendationUserPrompt(stat, sleepPercent, stepsPercent, outingPercent, socialPercent),
        )
        return parseRecommendation(raw)
    }

    private fun parseRecommendation(raw: String): ActivityRecommendationGeneratorPort.GeneratedRecommendation {
        val root = try {
            objectMapper.readTree(raw)
        } catch (e: JsonProcessingException) {
            throw InvalidRequestException("AI 활동 추천 생성에 실패했어요: 응답이 올바른 JSON이 아닙니다")
        }

        val parentMessage = root.path("parentMessage").takeIf { it.isTextual }?.asText()?.trim()
        val childMessage = root.path("childMessage").takeIf { it.isTextual }?.asText()?.trim()
        val actionTypeStr = root.path("actionType").takeIf { it.isTextual }?.asText()?.trim()

        if (parentMessage.isNullOrBlank() || childMessage.isNullOrBlank()) {
            throw InvalidRequestException("AI 활동 추천 생성에 실패했어요: 메시지가 비어 있습니다")
        }

        val actionType = DailyActivityRecommendationActionType.fromStringOrNull(actionTypeStr)
            ?: DailyActivityRecommendationActionType.NONE

        return ActivityRecommendationGeneratorPort.GeneratedRecommendation(
            parentMessage = parentMessage,
            childMessage = childMessage,
            actionType = actionType,
        )
    }

    private fun recommendationSystemPrompt(): String = """
        당신은 시니어 인지 건강과 가족 돌봄을 돕는 따뜻하고 지혜로운 AI 코치입니다.
        어르신의 오늘 하루 활동 데이터(수면 시간, 걸음 수, 외출 횟수, 사회적 연락 횟수)와 목표 대비 달성률(%)을 바탕으로,
        부모님 본인에게 건넬 맞춤형 활동 권유 메시지, 자녀에게 전달할 상태 알림 메시지, 그리고 상황에 맞는 추천 액션 타입(actionType)을 생성해주세요.

        [생성 규칙]
        1. parentMessage:
           - 부모님(어르신) 본인에게 직접 이야기하는 2인칭 존댓말(따뜻하고 격려하는 해요체)로 작성하세요.
           - 1~2문장으로 간결하고 정중하며 부담스럽지 않게 작성하세요.
           - 예: "오늘 날씨가 참 좋아요. 가벼운 산책 어떠세요?", "오늘 목표 걸음 수를 벌써 채우셨네요! 대단하세요."
        2. childMessage:
           - 자녀에게 부모님의 오늘 활동 상태와 안부를 알려주는 3인칭 존댓말로 작성하세요.
           - 1~2문장으로 사실적이고 다정하게 상황을 공유하세요.
           - 예: "어머니가 오늘 아직 외출을 못 하셨어요.", "아버지가 오늘 목표 걸음을 모두 달성하셨어요!"
        3. actionType:
           - 다음 4가지 중 활동 달성률을 종합 분석하여 가장 적절한 하나를 선택하세요:
             * "WALK": 걸음 수(stepsPercent) 또는 외출(outingPercent) 달성률이 낮아 산책이나 가벼운 걷기 활동이 필요할 때
             * "CALL": 사회적 연락(socialPercent) 달성률이 낮아 가족 간의 안부 전화나 연락이 필요할 때
             * "QUIZ": 전반적인 일상 활동 달성률이 양호하여 인지 자극을 위한 추억 퀴즈 풀기가 적절할 때
             * "NONE": 충분한 휴식이 필요하거나 특별한 행동 권유가 필요하지 않을 때

        응답은 반드시 아래 json 스키마를 따르는 단 하나의 JSON 객체로 작성하세요.
        {"parentMessage": "부모님께 드리는 메시지", "childMessage": "자녀에게 알리는 메시지", "actionType": "WALK"}
    """.trimIndent()

    private fun recommendationUserPrompt(
        stat: DailyActivityStat,
        sleepPercent: Int,
        stepsPercent: Int,
        outingPercent: Int,
        socialPercent: Int,
    ): String = """
        [오늘의 활동 요약]
        - 수면: ${stat.sleepMinutes}분 / 목표 ${stat.sleepGoalMinutes}분 (달성률 ${sleepPercent}%)
        - 걸음 수: ${stat.steps}걸음 / 목표 ${stat.stepsGoal}걸음 (달성률 ${stepsPercent}%)
        - 외출: ${stat.outingCount}회 / 목표 ${stat.outingGoal}회 (달성률 ${outingPercent}%)
        - 사회적 연락: ${stat.socialContactCount}회 / 목표 ${stat.socialGoal}회 (달성률 ${socialPercent}%)

        위 데이터를 분석하여 parentMessage, childMessage, actionType(WALK/CALL/QUIZ/NONE)을 json 형식으로 추천해주세요.
    """.trimIndent()
}
