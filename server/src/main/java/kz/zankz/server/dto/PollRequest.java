package kz.zankz.server.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Новое голосование (только ADMIN). kind: REFERENDUM | ELECTION | PROPOSAL
public record PollRequest(
        @NotBlank(message = "Укажите вид голосования") String kind,
        LocalDate eventDate,
        @NotBlank(message = "Название (kk) обязательно") String titleKk,
        @NotBlank(message = "Название (ru) обязательно") String titleRu,
        @NotBlank(message = "Название (en) обязательно") String titleEn,
        String descriptionKk, String descriptionRu, String descriptionEn,
        @NotBlank(message = "Вопрос (kk) обязателен") String questionKk,
        @NotBlank(message = "Вопрос (ru) обязателен") String questionRu,
        @NotBlank(message = "Вопрос (en) обязателен") String questionEn,
        @NotNull @Valid @Size(min = 2, max = 12, message = "Нужно от 2 до 12 вариантов") List<Option> options) {

    public record Option(@NotBlank String textKk, @NotBlank String textRu, @NotBlank String textEn) {
    }
}