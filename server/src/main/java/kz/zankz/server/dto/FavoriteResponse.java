package kz.zankz.server.dto;

import java.time.LocalDateTime;

import kz.zankz.server.model.Article;

// Ответ про избранное. Данные пользователя (пароль, ключ) сюда не попадают
public record FavoriteResponse(Long id, Article article, LocalDateTime createdAt) {
}