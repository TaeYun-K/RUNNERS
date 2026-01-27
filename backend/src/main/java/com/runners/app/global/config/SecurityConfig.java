package com.runners.app.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runners.app.global.exception.ApiErrorWriter;
import com.runners.app.global.exception.ErrorCode;
import com.runners.app.global.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper
    ) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((request, response, authException) -> {
                    ApiErrorWriter.write(response, objectMapper, 401, ErrorCode.UNAUTHORIZED, "Unauthorized");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    ApiErrorWriter.write(response, objectMapper, 403, ErrorCode.FORBIDDEN, "Forbidden");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**",
                    "/error",
                    "/error/**",
                    "/health",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                    ).permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/community/posts",
                    "/community/posts/search",
                    "/community/posts/*",
                    "/community/posts/*/comments"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
