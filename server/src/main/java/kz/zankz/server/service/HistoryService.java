package kz.zankz.server.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kz.zankz.server.dto.HistoryResponse;
import kz.zankz.server.model.Article;
import kz.zankz.server.model.HistoryEntry;
import kz.zankz.server.model.User;
import kz.zankz.server.repository.HistoryRepository;

// Логика истории просмотров
@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final ArticleService articleService;

    public HistoryService(HistoryRepository historyRepository, ArticleService articleService) {
        this.historyRepository = historyRepository;
        this.articleService = articleService;
    }

    public List<HistoryResponse> getRecent(User user) {
        return historyRepository.findTop20ByUserIdOrderByViewedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    // Отметить, что статья просмотрена. Повторный просмотр просто обновляет время
    public HistoryResponse record(User user, Long articleId) {
        Article article = articleService.getById(articleId);
        HistoryEntry entry = historyRepository.findByUserIdAndArticleId(user.getId(), articleId)
                .orElseGet(() -> {
                    HistoryEntry created = new HistoryEntry();
                    created.setUser(user);
                    created.setArticle(article);
                    return created;
                });
        entry.setViewedAt(LocalDateTime.now());
        return toResponse(historyRepository.save(entry));
    }

    // Очистить всю историю пользователя
    @Transactional
    public void clear(User user) {
        historyRepository.deleteByUserId(user.getId());
    }

    private HistoryResponse toResponse(HistoryEntry entry) {
        return new HistoryResponse(entry.getId(), entry.getArticle(), entry.getViewedAt());
    }
}