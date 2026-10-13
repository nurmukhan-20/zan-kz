package kz.zankz.server.dto;

import java.time.LocalDateTime;

import kz.zankz.server.model.Article;

public record FavoriteResponse(Long id, Article article, LocalDateTime createdAt) {
}