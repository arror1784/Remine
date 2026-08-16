package com.remine.call.domain

enum class CallStatus {
    CONNECTING,
    CONNECTED,
    ENDED,
    MISSED;

    companion object {
        fun from(name: String): CallStatus {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown CallStatus: $name")
        }
    }
}
