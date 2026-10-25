package kz.zankz.server.dto;

import java.util.List;

import kz.zankz.server.dto.AssistantResponse.ArticleRef;

// Готовый документ: название, текст и статьи, на которые он ссылается
public record DocumentResponse(String title, String text, List<ArticleRef> articles) {
}