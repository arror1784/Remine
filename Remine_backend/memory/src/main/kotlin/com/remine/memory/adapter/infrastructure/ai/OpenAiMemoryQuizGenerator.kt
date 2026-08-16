package com.remine.memory.adapter.infrastructure.ai

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.remine.client.openai.OpenAiClient
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.domain.MemoryPhoto
import org.springframework.stereotype.Component

@Component
class OpenAiMemoryQuizGenerator(
    private val openAiClient: OpenAiClient,
    private val objectMapper: ObjectMapper,
) : MemoryQuizGeneratorPort {

    override fun generateQuestions(photo: MemoryPhoto, count: Int): List<MemoryQuizGeneratorPort.GeneratedQuestion> {
        val raw = openAiClient.completeJson(systemPrompt(count), userPrompt(photo, count))
        return parse(raw, count)
    }

    private fun parse(raw: String, count: Int): List<MemoryQuizGeneratorPort.GeneratedQuestion> {
        val root = try {
            objectMapper.readTree(raw)
        } catch (e: JsonProcessingException) {
            throw InvalidRequestException("AI 퀴즈 생성에 실패했어요: 응답이 올바른 JSON이 아닙니다")
        }

        val questionsNode = root.path("questions")
        if (!questionsNode.isArray || questionsNode.isEmpty) {
            throw InvalidRequestException("AI 퀴즈 생성에 실패했어요: 응답에 questions 배열이 없습니다")
        }

        return questionsNode.take(count).map { toGeneratedQuestion(it) }
    }

    private fun toGeneratedQuestion(node: JsonNode): MemoryQuizGeneratorPort.GeneratedQuestion {
        val question = node.path("question").takeIf { it.isTextual }?.asText().orEmpty()
        val optionsNode = node.path("options")
        val options = if (optionsNode.isArray) optionsNode.mapNotNull { it.takeIf { o -> o.isTextual }?.asText() } else emptyList()
        val correctOptionIndex = node.path("correctOptionIndex").takeIf { it.isInt }?.asInt() ?: -1

        if (question.isBlank() || options.size < 2 || correctOptionIndex !in options.indices) {
            throw InvalidRequestException("AI 퀴즈 생성에 실패했어요: 문항 형식이 올바르지 않습니다")
        }
        return MemoryQuizGeneratorPort.GeneratedQuestion(question, options, correctOptionIndex)
    }

    // JSON mode requires the literal word "json" to appear in the prompts.
    private fun systemPrompt(count: Int): String = """
        당신은 어르신의 인지 건강을 돕는 따뜻한 회상 대화 도우미입니다.
        가족이 함께한 추억 사진을 보고, 어르신이 그 추억을 편안하게 떠올릴 수 있는
        객관식 회상 질문을 정확히 ${count}개 만들어 주세요.

        규칙:
        - 어렵거나 지엽적인 상식 문제가 아니라, 추억 자체를 부드럽게 되짚는 질문이어야 합니다.
          (예: "이 사진은 언제쯤 찍은 걸까요?", "이날 우리 가족은 어디에 갔을까요?")
        - 말투는 따뜻하고 쉽고 격려하는 존댓말로 씁니다.
        - 각 질문의 선택지는 정확히 4개이며, 정답 위치(correctOptionIndex)는 0부터 시작합니다.
        - 사진 제목과 추억 라벨에 담긴 정보에서 정답을 알 수 있게 만듭니다.

        응답은 반드시 아래 json 스키마를 그대로 따르는 JSON 객체 하나로만 작성하세요.
        {"questions": [{"question": "...", "options": ["...", "...", "...", "..."], "correctOptionIndex": 0}]}
    """.trimIndent()

    private fun userPrompt(photo: MemoryPhoto, count: Int): String = """
        추억 사진 제목: ${photo.title}
        추억 라벨: ${photo.memoryLabel}

        위 추억에 대한 회상 질문 ${count}개를 만들어 주세요.
    """.trimIndent()
}
