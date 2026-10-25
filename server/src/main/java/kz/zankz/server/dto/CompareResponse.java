package kz.zankz.server.dto;

import java.util.List;

import kz.zankz.server.dto.AssistantResponse.ArticleRef;

// Результат сравнения: текст ИИ и две статьи (кнопки «открыть статью»)
public record CompareResponse(String text, List<ArticleRef> articles) {
}