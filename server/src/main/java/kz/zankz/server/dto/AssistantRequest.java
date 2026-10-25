package kz.zankz.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Вопрос пользователя ИИ-помощнику. lang: kk | ru | en — на каком языке ответить.
public record AssistantRequest(
        @NotBlank(message = "Напишите вопрос") @Size(max = 1000, message = "Вопрос слишком длинный (до 1000 символов)") String question,
        String lang) {
}