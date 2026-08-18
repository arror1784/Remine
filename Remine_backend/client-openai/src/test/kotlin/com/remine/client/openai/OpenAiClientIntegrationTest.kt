package com.remine.client.openai

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

/**
 * Hits the real OpenAI Chat Completions API. Only runs when OPENAI_API_KEY is present in the
 * environment, so it's skipped in CI and for contributors without a key.
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiClientIntegrationTest {

    @Test
    fun `completeJson returns real content from the OpenAI API`() {
        val apiKey = System.getenv("OPENAI_API_KEY")
        val model = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini"
        val client = OpenAiClient(apiKey = apiKey, model = model)

        val result = client.completeJson(
            systemPrompt = "You reply only with json.",
            userPrompt = """Return a json object like {"status": "ok"}.""",
        )

        assertTrue(result.isNotBlank(), "expected non-blank content, got: $result")
        assertTrue(result.contains("status"), "expected the response to echo the requested key, got: $result")
    }
}
