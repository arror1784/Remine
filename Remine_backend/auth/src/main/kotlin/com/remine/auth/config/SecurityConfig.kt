package com.remine.auth.config

import com.remine.auth.jwt.JwtAuthenticationFilter
import com.remine.auth.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * No path-based role rules here (root CLAUDE.md rule: authorization only via
 * @PreAuthorize on controller methods, never hardcoded URL-path rules in a
 * filter). This chain only decides authenticated-vs-not.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint).and()
            .authorizeHttpRequests { auth ->
                auth
                    .antMatchers("/api/v1/auth/**").permitAll()
                    .antMatchers("/api/v1/users/signup").permitAll()
                    .antMatchers("/h2-console/**").permitAll()
                    .antMatchers("/actuator/health").permitAll()
                    .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            }
            .headers().frameOptions().disable().and()
            .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
