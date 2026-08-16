package com.remine.message.adapter.infrastructure.seed

import com.remine.message.application.port.outbound.QuickReplyRepositoryPort
import com.remine.message.domain.QuickReply
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class QuickReplySeeder(
    private val quickReplyRepositoryPort: QuickReplyRepositoryPort,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments?) {
        seedParentReplies()
        seedChildReplies()
    }

    private fun seedParentReplies() {
        if (!quickReplyRepositoryPort.existsByRole("PARENT")) {
            val parentReplies = listOf(
                "잘 있어💕",
                "알겠어~",
                "우리 딸 고마워 ❤️",
                "보고싶다",
                "전화할게",
            ).mapIndexed { index, label ->
                QuickReply(
                    role = "PARENT",
                    label = label,
                    sortOrder = index,
                )
            }
            quickReplyRepositoryPort.saveAll(parentReplies)
        }
    }

    private fun seedChildReplies() {
        if (!quickReplyRepositoryPort.existsByRole("CHILD")) {
            val childReplies = listOf(
                "잘 지내고 계세요? 💕",
                "오늘도 화이팅이에요!",
                "보고 싶어요 엄마 😊",
            ).mapIndexed { index, label ->
                QuickReply(
                    role = "CHILD",
                    label = label,
                    sortOrder = index,
                )
            }
            quickReplyRepositoryPort.saveAll(childReplies)
        }
    }
}
