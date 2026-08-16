package com.remine.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.integration.redis.util.RedisLockRegistry

/**
 * Session storage + distributed locks. The Redis Stream-based task queue
 * (wim:task:*) is not wired yet since there's no AI/knowledge service to
 * hand tasks to in this MVP scope (see root CLAUDE.md rule 2) — add a
 * StreamMessageListenerContainer bean here once that integration exists.
 */
@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        return template
    }

    @Bean
    fun redisLockRegistry(connectionFactory: RedisConnectionFactory): RedisLockRegistry =
        RedisLockRegistry(connectionFactory, "remine-lock")
}
