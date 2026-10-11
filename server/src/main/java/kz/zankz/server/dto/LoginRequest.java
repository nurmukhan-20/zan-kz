package kz.zankz.server.dto;

import jakarta.validation.constraints.NotBlank;

// Данные, которые приходят при входе
public record LoginRequest(
        @NotBlank(message = "Почта обязательна") String email,
        @NotBlank(message = "Пароль обязателен") String password) {
}