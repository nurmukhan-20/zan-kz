package kz.zankz.server.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Сравнение двух статей: articleIds — ровно 2 id статей, lang — язык ответа
public record CompareRequest(
        @NotNull(message = "Выберите две статьи") @Size(min = 2, max = 2, message = "Выберите ровно две статьи") List<Long> articleIds,
        String lang) {
}