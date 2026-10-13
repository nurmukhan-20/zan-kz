package kz.zankz.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kz.zankz.server.dto.FavoriteResponse;
import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Article;
import kz.zankz.server.model.Favorite;
import kz.zankz.server.model.User;
import kz.zankz.server.repository.FavoriteRepository;

// Тесты избранного
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    @DisplayName("Повторное добавление в избранное не создаёт дубликат")
    void addTwiceDoesNotDuplicate() {
        User user = user();
        Article article = new Article();
        article.setId(9L);
        Favorite existing = new Favorite();
        existing.setId(5L);
        existing.setUser(user);
        existing.setArticle(article);
        when(articleService.getById(9L)).thenReturn(article);
        when(favoriteRepository.findByUserIdAndArticleId(1L, 9L)).thenReturn(Optional.of(existing));

        FavoriteResponse response = favoriteService.add(user, 9L);

        assertThat(response.id()).isEqualTo(5L);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("Нельзя удалить из избранного то, чего там нет")
    void removeMissingThrows() {
        when(favoriteRepository.findByUserIdAndArticleId(1L, 3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.remove(user(), 3L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        return user;
    }
}