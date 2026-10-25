package kz.zankz.server.dto;

import java.util.List;

import kz.zankz.server.dto.AssistantResponse.ArticleRef;

// Разбор ситуации: summary — коротко что происходит с точки зрения закона,
// steps — что делать по шагам, articles — подходящие статьи и почему, note — предупреждение
public record SituationResponse(String summary, List<String> steps, List<Match> articles, String note) {

    public record Match(ArticleRef article, String why) {
    }
}