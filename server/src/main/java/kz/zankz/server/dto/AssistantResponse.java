package kz.zankz.server.dto;

import java.util.List;

import kz.zankz.server.model.Article;

// Ответ помощника: текст + статьи, на которые он опирался (в приложении — кнопки «открыть статью»)
public record AssistantResponse(String answer, List<ArticleRef> articles) {

    public record ArticleRef(Long id, String articleNumber,
            String titleKk, String titleRu, String titleEn,
            String lawTitleKk, String lawTitleRu, String lawTitleEn) {

        public static ArticleRef of(Article a) {
            return new ArticleRef(a.getId(), a.getArticleNumber(),
                    a.getTitleKk(), a.getTitleRu(), a.getTitleEn(),
                    a.getLaw().getTitleKk(), a.getLaw().getTitleRu(), a.getLaw().getTitleEn());
        }
    }
}