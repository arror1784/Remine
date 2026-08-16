package com.remine.memory.adapter.infrastructure.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.remine.client.openai.OpenAiClient
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.domain.MemoryPhoto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class StubOpenAiClient(private val response: String) : OpenAiClient(apiKey = "stub-key", model = "stub-model") {
    var lastSystemPrompt: String? = null
    var lastUserPrompt: String? = null

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
    fun `parses a well-formed response into generated questions`() {
        val json = """
            {"questions": [
              {"question": "이 사진은 언제쯤 찍은 걸까요?", "options": ["2020년 봄", "2021년 봄", "2022년 봄", "2023년 봄"], "correctOptionIndex": 2},
              {"question": "이날 우리 가족은 무엇을 했을까요?", "options": ["여행", "이사", "졸업식", "생신"], "correctOptionIndex": 0}
            ]}
        """.trimIndent()
        val (_, generator) = generatorFor(json)

        val questions = generator.generateQuestions(photo, count = 2)

        assertEquals(2, questions.size)
        assertEquals("이 사진은 언제쯤 찍은 걸까요?", questions[0].question)
        assertEquals(listOf("2020년 봄", "2021년 봄", "2022년 봄", "2023년 봄"), questions[0].options)
        assertEquals(2, questions[0].correctOptionIndex)
        assertEquals(0, questions[1].correctOptionIndex)
    }

    @Test
    fun `prompts carry the photo details and the literal word json required by JSON mode`() {
        val json = """{"questions": [{"question": "Q", "options": ["A", "B", "C", "D"], "correctOptionIndex": 1}]}"""
        val (client, generator) = generatorFor(json)

        generator.generateQuestions(photo, count = 1)

        assertTrue(client.lastSystemPrompt!!.contains("json"))
        assertTrue(client.lastUserPrompt!!.contains("가족 여행"))
        assertTrue(client.lastUserPrompt!!.contains("2022년 봄"))
    }

    @Test
    fun `truncates to the requested count when the model returns extras`() {
        val json = """
            {"questions": [
              {"question": "Q1", "options": ["A", "B", "C", "D"], "correctOptionIndex": 0},
              {"question": "Q2", "options": ["A", "B", "C", "D"], "correctOptionIndex": 1},
              {"question": "Q3", "options": ["A", "B", "C", "D"], "correctOptionIndex": 2}
            ]}
        """.trimIndent()
        val (_, generator) = generatorFor(json)

        assertEquals(2, generator.generateQuestions(photo, count = 2).size)
    }

    @Test
    fun `throws when the response is not valid JSON`() {
        val (_, generator) = generatorFor("sorry, I can't do that")

        assertThrows<InvalidRequestException> { generator.generateQuestions(photo) }
    }

    @Test
    fun `throws when the questions array is missing or empty`() {
        assertThrows<InvalidRequestException> { generatorFor("""{"result": "ok"}""").second.generateQuestions(photo) }
        assertThrows<InvalidRequestException> { generatorFor("""{"questions": []}""").second.generateQuestions(photo) }
    }

    @Test
    fun `throws when correctOptionIndex is out of range`() {
        val json = """{"questions": [{"question": "Q", "options": ["A", "B", "C", "D"], "correctOptionIndex": 7}]}"""
        val (_, generator) = generatorFor(json)

        assertThrows<InvalidRequestException> { generator.generateQuestions(photo, count = 1) }
    }

    @Test
    fun `throws when a question is missing its text or options`() {
        val missingText = """{"questions": [{"question": "", "options": ["A", "B", "C", "D"], "correctOptionIndex": 0}]}"""
        val missingOptions = """{"questions": [{"question": "Q", "options": ["A"], "correctOptionIndex": 0}]}"""

        assertThrows<InvalidRequestException> { generatorFor(missingText).second.generateQuestions(photo, count = 1) }
        assertThrows<InvalidRequestException> { generatorFor(missingOptions).second.generateQuestions(photo, count = 1) }
    }
}
