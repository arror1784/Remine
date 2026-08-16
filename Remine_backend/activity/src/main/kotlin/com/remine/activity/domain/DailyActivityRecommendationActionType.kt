package com.remine.activity.domain

enum class DailyActivityRecommendationActionType {
    WALK,
    CALL,
    QUIZ,
    NONE,
    ;

    companion object {
        fun fromStringOrNull(value: String?): DailyActivityRecommendationActionType? =
            values().firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) }
    }
}
