package com.speakmate.backend.config;

import com.speakmate.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Explicitly declare the standard multipart resolver so Spring processes
     * multipart/form-data requests (e.g. /api/chat/voice file uploads) before
     * the security filter chain inspects them. Without this bean declaration
     * some servlet containers defer multipart parsing past the security filters,
     * causing 400 / missing-parameter errors on file upload endpoints.
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS using WebMvcConfigurer
            .cors(org.springframework.security.config.Customizer.withDefaults())
            // Disable CSRF — we use stateless JWT, no session cookies
            .csrf(csrf -> csrf.disable())

            // Disable default HTTP Basic and form login
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(authorize -> authorize
                // ── Public: auth endpoints ──────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()

                // ── Public: frontend static files ───────────────────────────
                .requestMatchers("/", "/index.html", "/*.html", "/favicon.ico").permitAll()

                // ── Protected: all API endpoints require a valid JWT ─────────
                .requestMatchers("/api/**").authenticated()

                // ── Everything else (CSS, JS, images, fonts) ────────────────
                .anyRequest().permitAll()
            );

        // Insert JWT filter before the standard username/password filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
