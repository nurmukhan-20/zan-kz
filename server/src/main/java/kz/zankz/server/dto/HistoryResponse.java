package kz.zankz.server.dto;

import java.time.LocalDateTime;

import kz.zankz.server.model.Article;

// Ответ про историю просмотров
public record HistoryResponse(Long id, Article article, LocalDateTime viewedAt) {
}