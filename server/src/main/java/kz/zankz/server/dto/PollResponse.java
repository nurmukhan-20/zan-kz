package kz.zankz.server.dto;

import java.time.LocalDate;
import java.util.List;

// Голосование с результатами опроса в приложении.
// myOptionId — какой вариант выбрал этот пользователь (null — ещё не голосовал)
public record PollResponse(
        Long id, String kind, LocalDate eventDate, boolean active,
        String titleKk, String titleRu, String titleEn,
        String descriptionKk, String descriptionRu, String descriptionEn,
        String questionKk, String questionRu, String questionEn,
        List<Option> options, long total, Long myOptionId) {

    public record Option(Long id, String textKk, String textRu, String textEn, long votes) {
    }
}