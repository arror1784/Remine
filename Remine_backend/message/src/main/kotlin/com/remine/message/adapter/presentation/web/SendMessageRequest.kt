package com.remine.message.adapter.presentation.web

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class SendMessageRequest(
    @field:NotBlank(message = "Message text must not be blank")
    @field:Size(max = 1000, message = "Message text cannot exceed 1000 characters")
    val text: String = "",
    val quickReplyKey: String? = null,
)
