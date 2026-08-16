package com.remine.auth.jwt

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms:604800000}") private val expirationMs: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: UUID, role: Role, pairedUserId: UUID?): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role.name)
            .claim("pairedUserId", pairedUserId?.toString())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun parse(token: String): RemineUserPrincipal? {
        return try {
            val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
            RemineUserPrincipal(
                userId = UUID.fromString(claims.subject),
                role = Role.valueOf(claims["role"] as String),
                pairedUserId = (claims["pairedUserId"] as String?)?.let(UUID::fromString),
            )
        } catch (ex: Exception) {
            null
        }
    }
}
