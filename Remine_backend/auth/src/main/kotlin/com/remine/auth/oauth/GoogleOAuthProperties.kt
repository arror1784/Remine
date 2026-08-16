package com.remine.auth.oauth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

/**
 * Values come from Vault-injected env vars (GOOGLE_OAUTH_CLIENT_ID /
 * GOOGLE_OAUTH_CLIENT_SECRET), never hardcoded. The actual
 * /api/v1/auth/google/callback handler lives in the `user` module, since it
 * needs to create/look up a User row — this class only holds the client
 * credentials config.
 */
@Configuration
@ConfigurationProperties(prefix = "google.oauth")
data class GoogleOAuthProperties @ConstructorBinding constructor(
    val clientId: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "",
)
