package kz.zankz.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Данные, которые приходят при регистрации
public record RegisterRequest(
        @NotBlank(message = "Имя обязательно") String name,
        @NotBlank(message = "Почта обязательна") @Email(message = "Почта указана неверно") String email,
        @NotBlank(message = "Пароль обязателен") @Size(min = 6, message = "Пароль должен быть не короче 6 символов") String password) {
}