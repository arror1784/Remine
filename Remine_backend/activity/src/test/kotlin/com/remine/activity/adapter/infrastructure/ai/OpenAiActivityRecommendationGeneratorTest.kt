package com.remine.activity.adapter.infrastructure.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.remine.activity.domain.DailyActivityRecommendationActionType
import com.remine.activity.domain.DailyActivityStat
import com.remine.client.openai.OpenAiClient
import com.remine.common.domain.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.util.UUID

class StubOpenAiClient(private var response: String) : OpenAiClient(apiKey = "stub-key", model = "stub-model") {
    var lastSystemPrompt: String? = null
    var lastUserPrompt: String? = null

    fun setResponse(newResponse: String) {
        this.response = newResponse
    }

    override fun completeJson(systemPrompt: String, userPrompt: String): String {
        lastSystemPrompt = systemPrompt
        lastUserPrompt = userPrompt
        return response
    }
}

class OpenAiActivityRecommendationGeneratorTest {

    private val sampleStat = DailyActivityStat(
        id = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        statDate = LocalDate.now(),
        sleepMinutes = 420,
        steps = 3000,
        outingCount = 0,
        socialContactCount = 1,
        sleepGoalMinutes = 480,
        stepsGoal = 8000,
        outingGoal = 1,
        socialGoal = 1,
    )

    private fun generatorFor(response: String, client: StubOpenAiClient = StubOpenAiClient(response)) =
        client to OpenAiActivityRecommendationGenerator(client, ObjectMapper())

    @Test
    fun `generateRecommendation parses well-formed JSON response`() {
        val json = """
            {
              "parentMessage": "오늘 날씨가 참 좋아요. 가벼운 산책 어떠세요?",
              "childMessage": "어머니가 오늘 아직 외출을 못 하셨어요.",
              "actionType": "WALK"
            }
        """.trimIndent()
        val (client, generator) = generatorFor(json)

        val result = generator.generateRecommendation(
            stat = sampleStat,
            sleepPercent = 87,
            stepsPercent = 37,
            outingPercent = 0,
            socialPercent = 100,
        )

        assertEquals("오늘 날씨가 참 좋아요. 가벼운 산책 어떠세요?", result.parentMessage)
        assertEquals("어머니가 오늘 아직 외출을 못 하셨어요.", result.childMessage)
        assertEquals(DailyActivityRecommendationActionType.WALK, result.actionType)

        assertTrue(client.lastSystemPrompt!!.contains("json"))
        assertTrue(client.lastUserPrompt!!.contains("420"))
        assertTrue(client.lastUserPrompt!!.contains("3000"))
        assertTrue(client.lastUserPrompt!!.contains("37%"))
    }

    @Test
    fun `generateRecommendation parses all action types case-insensitively`() {
        val types = listOf(
            "walk" to DailyActivityRecommendationActionType.WALK,
            "CALL" to DailyActivityRecommendationActionType.CALL,
            "quiz" to DailyActivityRecommendationActionType.QUIZ,
            "NONE" to DailyActivityRecommendationActionType.NONE,
        )

        for ((typeStr, expectedType) in types) {
            val json = """
                {
                  "parentMessage": "부모님 메시지",
                  "childMessage": "자녀 메시지",
                  "actionType": "$typeStr"
                }
            """.trimIndent()
            val (_, generator) = generatorFor(json)

            val result = generator.generateRecommendation(
                stat = sampleStat,
                sleepPercent = 100,
                stepsPercent = 100,
                outingPercent = 100,
                socialPercent = 100,
            )
            assertEquals(expectedType, result.actionType)
        }
    }

    @Test
    fun `generateRecommendation falls back to NONE for unrecognized actionType`() {
        val json = """
            {
              "parentMessage": "부모님 메시지",
              "childMessage": "자녀 메시지",
              "actionType": "UNKNOWN_ACTION"
            }
        """.trimIndent()
        val (_, generator) = generatorFor(json)

        val result = generator.generateRecommendation(
            stat = sampleStat,
            sleepPercent = 50,
            stepsPercent = 50,
            outingPercent = 50,
            socialPercent = 50,
        )
        assertEquals(DailyActivityRecommendationActionType.NONE, result.actionType)
    }

    @Test
    fun `generateRecommendation throws InvalidRequestException when JSON is invalid`() {
        val (_, generator) = generatorFor("not-json")

        assertThrows<InvalidRequestException> {
            generator.generateRecommendation(
                stat = sampleStat,
                sleepPercent = 50,
                stepsPercent = 50,
                outingPercent = 50,
                socialPercent = 50,
            )
        }
    }

    @Test
    fun `generateRecommendation throws InvalidRequestException when messages are blank or missing`() {
        val emptyParentJson = """{"parentMessage": "", "childMessage": "자녀 메시지", "actionType": "WALK"}"""
        assertThrows<InvalidRequestException> {
            generatorFor(emptyParentJson).second.generateRecommendation(sampleStat, 50, 50, 50, 50)
        }

        val missingChildJson = """{"parentMessage": "부모님 메시지", "actionType": "WALK"}"""
        assertThrows<InvalidRequestException> {
            generatorFor(missingChildJson).second.generateRecommendation(sampleStat, 50, 50, 50, 50)
        }
    }
}
