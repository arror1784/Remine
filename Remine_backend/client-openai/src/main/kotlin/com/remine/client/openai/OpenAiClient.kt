package com.remine.client.openai

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

/**
 * Thin wrapper over OpenAI's Chat Completions API. Domain-agnostic on purpose:
 * it only knows "prompts in, JSON string out" — callers own the schema.
 */
@Component
class OpenAiClient(
    @Value("\${openai.api-key}") private val apiKey: String,
    @Value("\${openai.model:gpt-4o-mini}") private val model: String,
    private val restTemplate: RestTemplate = RestTemplate(),
) {

    /**
     * Runs a chat completion in JSON mode and returns the raw `choices[0].message.content`.
     * JSON mode requires the literal word "json" somewhere in the prompts — the caller must supply it.
     */
    fun completeJson(systemPrompt: String, userPrompt: String): String {
        if (apiKey.isBlank()) {
            throw OpenAiClientException("OpenAI API key is not configured (set the OPENAI_API_KEY environment variable)")
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(apiKey)
        }
        val body = mapOf(
            "model" to model,
            "response_format" to mapOf("type" to "json_object"),
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt),
            ),
        )

        val response = try {
            restTemplate.postForEntity(CHAT_COMPLETIONS_URL, HttpEntity(body, headers), JsonNode::class.java)
        } catch (e: RestClientResponseException) {
            throw OpenAiClientException(
                "OpenAI chat completion failed with status ${e.rawStatusCode}: ${e.responseBodyAsString}",
                e,
            )
        } catch (e: RestClientException) {
            throw OpenAiClientException("OpenAI chat completion request failed: ${e.message}", e)
        }

        val content = response.body
            ?.path("choices")?.firstOrNull()
            ?.path("message")?.path("content")
            ?.takeIf { it.isTextual }?.asText()

        if (content.isNullOrBlank()) {
            throw OpenAiClientException("OpenAI chat completion returned no message content: ${response.body}")
        }
        return content
    }

    private companion object {
        const val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
    }
}
