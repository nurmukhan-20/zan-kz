package kz.zankz.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Запрос на документ. type: VACATION | RESIGNATION | CLAIM | RECEIPT
// to — кому (организация, руководитель), from — от кого, details — подробности своими словами
public record DocumentRequest(
        @NotBlank(message = "Выберите тип документа") String type,
        @Size(max = 300, message = "Поле «Кому» слишком длинное") String to,
        @Size(max = 300, message = "Поле «От кого» слишком длинное") String from,
        @NotBlank(message = "Опишите подробности") @Size(max = 1500, message = "Подробности слишком длинные (до 1500 символов)") String details,
        String lang) {
}