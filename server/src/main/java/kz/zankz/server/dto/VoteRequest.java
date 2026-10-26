package kz.zankz.server.dto;

import jakarta.validation.constraints.NotNull;

// Голос: id выбранного варианта
public record VoteRequest(@NotNull(message = "Выберите вариант") Long optionId) {
}