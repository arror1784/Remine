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

    override fun generateDraftQuestions(photo: MemoryPhoto, count: Int): List<String> {
        val raw = openAiClient.completeJson(
            draftQuestionsSystemPrompt(count),
            draftQuestionsUserPrompt(photo, count),
        )
        return parseDraftQuestions(raw, count)
    }

    override fun generateDistractors(
        items: List<MemoryQuizGeneratorPort.QuestionAndAnswer>,
    ): List<MemoryQuizGeneratorPort.GeneratedDistractors> {
        if (items.isEmpty()) return emptyList()

        val raw = openAiClient.completeJson(
            distractorsSystemPrompt(),
            distractorsUserPrompt(items),
        )
        return parseDistractors(raw, items)
    }

    private fun parseDraftQuestions(raw: String, count: Int): List<String> {
        val root = parseJson(raw, "질문")
        val questionsNode = root.path("questions")
        if (!questionsNode.isArray || questionsNode.isEmpty) {
            throw InvalidRequestException("AI 질문 생성에 실패했어요: 응답에 questions 배열이 없습니다")
        }

        val questions = questionsNode.mapNotNull { node ->
            node.takeIf { it.isTextual }?.asText()?.trim()?.takeIf { it.isNotBlank() }
        }

        if (questions.isEmpty()) {
            throw InvalidRequestException("AI 질문 생성에 실패했어요: 유효한 질문 문항이 없습니다")
        }

        return questions.take(count)
    }

    private fun parseDistractors(
        raw: String,
        expectedItems: List<MemoryQuizGeneratorPort.QuestionAndAnswer>,
    ): List<MemoryQuizGeneratorPort.GeneratedDistractors> {
        val root = parseJson(raw, "보기")
        val itemsNode = root.path("items")
        if (!itemsNode.isArray || itemsNode.isEmpty) {
            throw InvalidRequestException("AI 보기 생성에 실패했어요: 응답에 items 배열이 없습니다")
        }

        val parsedList = mutableListOf<MemoryQuizGeneratorPort.GeneratedDistractors>()

        for ((index, expected) in expectedItems.withIndex()) {
            val node = if (index < itemsNode.size()) itemsNode.get(index) else null
            val distractorsNode = node?.path("distractors")
            val distractors = if (distractorsNode != null && distractorsNode.isArray) {
                distractorsNode.mapNotNull { it.takeIf { d -> d.isTextual }?.asText()?.trim()?.takeIf { d -> d.isNotBlank() } }
            } else {
                emptyList()
            }

            if (distractors.size < 3) {
                throw InvalidRequestException("AI 보기 생성에 실패했어요: 질문 '${expected.question}'에 대한 오답 보기가 부족합니다")
            }

            val questionText = node?.path("question")?.takeIf { it.isTextual }?.asText()?.takeIf { it.isNotBlank() }
                ?: expected.question

            parsedList.add(
                MemoryQuizGeneratorPort.GeneratedDistractors(
                    question = questionText,
                    distractors = distractors.take(3),
                ),
            )
        }

        return parsedList
    }

    private fun parseJson(raw: String, stepName: String): JsonNode {
        return try {
            objectMapper.readTree(raw)
        } catch (e: JsonProcessingException) {
            throw InvalidRequestException("AI $stepName 생성에 실패했어요: 응답이 올바른 JSON이 아닙니다")
        }
    }

    // JSON mode requires the literal word "json" to appear in the prompts.
    private fun draftQuestionsSystemPrompt(count: Int): String = """
        당신은 어르신의 인지 건강을 돕는 따뜻한 회상 대화 도우미입니다.
        가족이 함께한 추억 사진의 정보(제목, 추억 라벨)를 보고, 어르신이 그 추억을 편안하게 떠올릴 수 있는
        회상 질문을 정확히 ${count}개 만들어 주세요.

        규칙:
        - 어렵거나 지엽적인 상식 문제가 아니라, 추억 자체를 부드럽게 되짚는 질문이어야 합니다.
          (예: "이 사진은 언제쯤 찍은 걸까요?", "이날 우리 가족은 어디에 갔을까요?", "이 사진에서 가족들과 무엇을 먹었을까요?")
        - 말투는 어르신께 여쭙는 따뜻하고 쉽고 격려하는 존댓말로 씁니다.
        - 보기(선택지)나 정답은 절대 만들지 마세요. 순수 질문 문장만 작성합니다.

        응답은 반드시 아래 json 스키마를 그대로 따르는 JSON 객체 하나로만 작성하세요.
        {"questions": ["질문 1", "질문 2", "질문 3"]}
    """.trimIndent()

    private fun draftQuestionsUserPrompt(photo: MemoryPhoto, count: Int): String = """
        추억 사진 제목: ${photo.title}
        추억 라벨: ${photo.memoryLabel}

        위 추억에 대한 회상 질문 ${count}개를 만들어 주세요.
    """.trimIndent()

    private fun distractorsSystemPrompt(): String = """
        당신은 어르신의 인지 건강을 돕는 따뜻한 회상 대화 도우미입니다.
        가족이 제공한 질문과 실제 정답을 바탕으로, 4지선다 객관식 퀴즈에 들어갈
        그럴듯하지만 틀린 오답 보기(distractor)를 각 질문마다 정확히 3개씩 만들어 주세요.

        규칙:
        - 각 질문마다 정답과 자연스럽게 어울리며 헷갈릴 수 있는 오답 보기를 정확히 3개씩 제시하세요.
        - 오답 보기는 정답과 동일한 범주(장소면 다른 장소, 연도/계절이면 다른 연도/계절, 활동이면 다른 활동, 인물이면 다른 가족/인물)여야 합니다.
        - 정답과 겹치거나 사실상 같은 의미인 오답을 만들지 마세요.
        - 단어 길이와 말투는 정답의 톤앤매너와 어울리게 작성하세요.

        응답은 반드시 아래 json 스키마를 그대로 따르는 JSON 객체 하나로만 작성하세요.
        {"items": [{"question": "질문 내용", "distractors": ["오답 1", "오답 2", "오답 3"]}]}
    """.trimIndent()

    private fun distractorsUserPrompt(items: List<MemoryQuizGeneratorPort.QuestionAndAnswer>): String = buildString {
        appendLine("다음 질문들과 각 질문의 실제 정답입니다:")
        items.forEachIndexed { index, item ->
            appendLine("${index + 1}. 질문: ${item.question}")
            appendLine("   정답: ${item.answer}")
        }
        appendLine()
        appendLine("각 질문에 대해 그럴듯한 오답 보기(distractors) 3개씩을 만들어 주세요.")
    }
}
