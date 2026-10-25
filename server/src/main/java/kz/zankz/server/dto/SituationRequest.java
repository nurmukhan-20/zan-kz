package kz.zankz.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Жизненная ситуация своими словами + язык ответа
public record SituationRequest(
        @NotBlank(message = "Опишите ситуацию") @Size(max = 1500, message = "Описание слишком длинное (до 1500 символов)") String situation,
        String lang) {
}