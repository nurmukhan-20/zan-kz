package kz.zankz.server.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import kz.zankz.server.security.TokenFilter;

// Правила доступа: кто что может делать
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Общие данные: читать могут все, менять — только ADMIN
    private static final String[] PUBLIC_DATA = {
            "/api/categories/**", "/api/laws/**", "/api/articles/**", "/api/terms/**", "/api/notifications/**"
    };

    // Страница документации Swagger
    private static final String[] SWAGGER = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    // Шифрование паролей
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenFilter tokenFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // регистрация, вход и документация доступны всем
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/error").permitAll()
                        .requestMatchers(SWAGGER).permitAll()
                        // читать общие данные можно без входа
                        .requestMatchers(HttpMethod.GET, PUBLIC_DATA).permitAll()
                        // добавлять, менять и удалять может только администратор
                        .requestMatchers(PUBLIC_DATA).hasRole("ADMIN")
                        // всё остальное (избранное, история) — только после входа
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> write(response, 401, "Сначала войдите в аккаунт"))
                        .accessDeniedHandler((request, response, ex) -> write(response, 403, "Недостаточно прав")))
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Понятный ответ, когда доступ запрещён
    private static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}