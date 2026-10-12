package kz.zankz.server.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import kz.zankz.server.dto.FavoriteResponse;
import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Article;
import kz.zankz.server.model.Favorite;
import kz.zankz.server.model.User;
import kz.zankz.server.repository.FavoriteRepository;

// Логика избранного
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ArticleService articleService;

    public FavoriteService(FavoriteRepository favoriteRepository, ArticleService articleService) {
        this.favoriteRepository = favoriteRepository;
        this.articleService = articleService;
    }

    public List<FavoriteResponse> getAll(User user) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    // Добавить в избранное. Если статья уже там — просто вернуть её
    public FavoriteResponse add(User user, Long articleId) {
        Article article = articleService.getById(articleId);
        Favorite favorite = favoriteRepository.findByUserIdAndArticleId(user.getId(), articleId)
                .orElseGet(() -> {
                    Favorite created = new Favorite();
                    created.setUser(user);
                    created.setArticle(article);
                    created.setCreatedAt(LocalDateTime.now());
                    return favoriteRepository.save(created);
                });
        return toResponse(favorite);
    }

    public void remove(User user, Long articleId) {
        Favorite favorite = favoriteRepository.findByUserIdAndArticleId(user.getId(), articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Этой статьи нет в избранном"));
        favoriteRepository.delete(favorite);
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        return new FavoriteResponse(favorite.getId(), favorite.getArticle(), favorite.getCreatedAt());
    }
}