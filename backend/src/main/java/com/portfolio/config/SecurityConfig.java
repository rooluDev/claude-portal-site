package com.portfolio.config;

import com.portfolio.auth.jwt.JwtAuthEntryPoint;
import com.portfolio.auth.jwt.JwtAuthFilter;
import com.portfolio.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CorsProperties corsProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(jwtAuthEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // 인증 API: 누구나 접근 가능
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()

                // 파일 서빙: 누구나 접근 가능
                .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()

                // 공지사항 CUD: ADMIN 전용
                .requestMatchers(HttpMethod.POST, "/api/notice").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/notice/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/notice/**").hasRole("ADMIN")

                // 문의 답변 CUD: ADMIN 전용
                .requestMatchers(HttpMethod.POST, "/api/inquiry-answer/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inquiry-answer/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inquiry-answer/**").hasRole("ADMIN")

                // 게시글 CUD: USER 이상
                .requestMatchers(HttpMethod.POST, "/api/free").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/free/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/free/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/gallery").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/gallery/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/gallery/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/inquiry").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inquiry/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inquiry/**").hasAnyRole("USER", "ADMIN")

                // 댓글 CUD: USER 이상
                .requestMatchers(HttpMethod.POST, "/api/comments").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/comments/**").hasAnyRole("USER", "ADMIN")

                // GET 요청: 누구나 접근 가능 (비밀글·나의 문의내역은 Service에서 추가 검증)
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                // 그 외: 인증 필요
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthFilter(jwtProvider),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(corsProperties.getAllowedOrigins()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
