package com.remine.client.openai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class OpenAiClientTest {

    private val restTemplate = RestTemplate()
    private val mockServer = MockRestServiceServer.createServer(restTemplate)
    private val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"

    @Test
    fun `blank api key throws before making any HTTP call`() {
        val client = OpenAiClient(apiKey = "", model = "gpt-4o-mini", restTemplate = restTemplate)

        val exception = assertThrows<OpenAiClientException> {
            client.completeJson("system", "user")
        }
        assertTrue(exception.message!!.contains("not configured"))
    }

    @Test
    fun `success response returns the message content`() {
        val client = OpenAiClient(apiKey = "test-key", model = "gpt-4o-mini", restTemplate = restTemplate)
        mockServer.expect(requestTo(CHAT_COMPLETIONS_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess(
                    """{"choices":[{"message":{"content":"{\"result\":\"ok\"}"}}]}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val result = client.completeJson("system json prompt", "user json prompt")

        assertEquals("""{"result":"ok"}""", result)
        mockServer.verify()
    }

    @Test
    fun `non-2xx response is wrapped in OpenAiClientException`() {
        val client = OpenAiClient(apiKey = "test-key", model = "gpt-4o-mini", restTemplate = restTemplate)
        mockServer.expect(requestTo(CHAT_COMPLETIONS_URL))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("""{"error":"invalid key"}"""))

        val exception = assertThrows<OpenAiClientException> {
            client.completeJson("system json prompt", "user json prompt")
        }
        assertTrue(exception.message!!.contains("401"))
        mockServer.verify()
    }

    @Test
    fun `response with no message content throws OpenAiClientException`() {
        val client = OpenAiClient(apiKey = "test-key", model = "gpt-4o-mini", restTemplate = restTemplate)
        mockServer.expect(requestTo(CHAT_COMPLETIONS_URL))
            .andRespond(withSuccess("""{"choices":[]}""", MediaType.APPLICATION_JSON))

        val exception = assertThrows<OpenAiClientException> {
            client.completeJson("system json prompt", "user json prompt")
        }
        assertTrue(exception.message!!.contains("no message content"))
        mockServer.verify()
    }
}
