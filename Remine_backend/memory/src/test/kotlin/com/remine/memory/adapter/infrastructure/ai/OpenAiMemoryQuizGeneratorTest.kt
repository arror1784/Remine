package com.remine.memory.adapter.infrastructure.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.remine.client.openai.OpenAiClient
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.domain.MemoryPhoto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

class OpenAiMemoryQuizGeneratorTest {

    private val photo = MemoryPhoto(
        id = UUID.randomUUID(),
        ownerUserId = UUID.randomUUID(),
        uploadedByUserId = UUID.randomUUID(),
        title = "가족 여행",
        photoUrl = "https://example.com/photo.jpg",
        memoryLabel = "2022년 봄",
    )

    private fun generatorFor(response: String, client: StubOpenAiClient = StubOpenAiClient(response)) =
        client to OpenAiMemoryQuizGenerator(client, ObjectMapper())

    @Test
    fun `generateDraftQuestions parses well-formed response into list of question strings`() {
        val json = """
            {"questions": [
              "이 사진은 언제쯤 찍은 걸까요?",
              "이날 우리 가족은 어디에 갔을까요?"
            ]}
        """.trimIndent()
        val (_, generator) = generatorFor(json)

        val questions = generator.generateDraftQuestions(photo, count = 2)

        assertEquals(2, questions.size)
        assertEquals("이 사진은 언제쯤 찍은 걸까요?", questions[0])
        assertEquals("이날 우리 가족은 어디에 갔을까요?", questions[1])
    }

    @Test
    fun `generateDraftQuestions prompts carry photo details and literal word json`() {
        val json = """{"questions": ["Q1", "Q2"]}"""
        val (client, generator) = generatorFor(json)

        generator.generateDraftQuestions(photo, count = 2)

        assertTrue(client.lastSystemPrompt!!.contains("json"))
        assertTrue(client.lastUserPrompt!!.contains("가족 여행"))
        assertTrue(client.lastUserPrompt!!.contains("2022년 봄"))
    }

    @Test
    fun `generateDraftQuestions truncates to requested count when model returns extras`() {
        val json = """{"questions": ["Q1", "Q2", "Q3", "Q4"]}"""
        val (_, generator) = generatorFor(json)

        val questions = generator.generateDraftQuestions(photo, count = 3)
        assertEquals(3, questions.size)
    }

    @Test
    fun `generateDraftQuestions throws when response is not valid JSON`() {
        val (_, generator) = generatorFor("invalid json string")

        assertThrows<InvalidRequestException> { generator.generateDraftQuestions(photo) }
    }

    @Test
    fun `generateDraftQuestions throws when questions array is missing or empty`() {
        assertThrows<InvalidRequestException> { generatorFor("""{"result": "ok"}""").second.generateDraftQuestions(photo) }
        assertThrows<InvalidRequestException> { generatorFor("""{"questions": []}""").second.generateDraftQuestions(photo) }
    }

    @Test
    fun `generateDistractors parses well-formed distractors response`() {
        val json = """
            {"items": [
              {
                "question": "이 사진은 어디서 찍었을까요?",
                "distractors": ["경주 불국사", "부산 해운대", "강릉 경포대"]
              },
              {
                "question": "이때 누구와 함께 갔나요?",
                "distractors": ["고향 친구들", "직장 동료들", "등산 동호회"]
              }
            ]}
        """.trimIndent()
        val (client, generator) = generatorFor(json)

        val items = listOf(
            MemoryQuizGeneratorPort.QuestionAndAnswer(
                question = "이 사진은 어디서 찍었을까요?",
                answer = "제주도 성산일출봉",
            ),
            MemoryQuizGeneratorPort.QuestionAndAnswer(
                question = "이때 누구와 함께 갔나요?",
                answer = "큰딸과 손자",
            ),
        )

        val result = generator.generateDistractors(items)

        assertEquals(2, result.size)
        assertEquals("이 사진은 어디서 찍었을까요?", result[0].question)
        assertEquals(listOf("경주 불국사", "부산 해운대", "강릉 경포대"), result[0].distractors)
        assertEquals("이때 누구와 함께 갔나요?", result[1].question)
        assertEquals(listOf("고향 친구들", "직장 동료들", "등산 동호회"), result[1].distractors)

        assertTrue(client.lastSystemPrompt!!.contains("json"))
        assertTrue(client.lastUserPrompt!!.contains("제주도 성산일출봉"))
        assertTrue(client.lastUserPrompt!!.contains("큰딸과 손자"))
    }

    @Test
    fun `generateDistractors returns empty list when given empty items`() {
        val (_, generator) = generatorFor("""{"items": []}""")
        val result = generator.generateDistractors(emptyList())
        assertEquals(0, result.size)
    }

    @Test
    fun `generateDistractors throws when response has fewer than 3 distractors`() {
        val json = """
            {"items": [
              {
                "question": "Q1",
                "distractors": ["D1", "D2"]
              }
            ]}
        """.trimIndent()
        val (_, generator) = generatorFor(json)

        val items = listOf(MemoryQuizGeneratorPort.QuestionAndAnswer(question = "Q1", answer = "A1"))
        assertThrows<InvalidRequestException> { generator.generateDistractors(items) }
    }

    @Test
    fun `generateDistractors throws when response is invalid json or missing items`() {
        val items = listOf(MemoryQuizGeneratorPort.QuestionAndAnswer(question = "Q1", answer = "A1"))

        assertThrows<InvalidRequestException> { generatorFor("invalid json").second.generateDistractors(items) }
        assertThrows<InvalidRequestException> { generatorFor("""{"result": "ok"}""").second.generateDistractors(items) }
        assertThrows<InvalidRequestException> { generatorFor("""{"items": []}""").second.generateDistractors(items) }
    }
}
