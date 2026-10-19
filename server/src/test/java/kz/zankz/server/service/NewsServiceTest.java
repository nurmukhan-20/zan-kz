package kz.zankz.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kz.zankz.server.model.News;
import kz.zankz.server.repository.NewsRepository;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("«Последние» — новости по дате")
    void latestSortsByDate() {
        News news = news("Последняя");
        when(newsRepository.findAllByOrderByPublishedAtDescPublishedTimeDescIdDesc()).thenReturn(List.of(news));

        assertThat(newsService.getAll("latest")).containsExactly(news);
    }

    @Test
    @DisplayName("«Популярные» — новости по просмотрам")
    void popularSortsByViews() {
        News news = news("Популярная");
        when(newsRepository.findAllByOrderByViewsDescPublishedAtDescPublishedTimeDesc()).thenReturn(List.of(news));

        assertThat(newsService.getAll("popular")).containsExactly(news);
    }

    @Test
    @DisplayName("Открытие новости прибавляет один просмотр")
    void viewAddsOne() {
        News news = news("Новость");
        news.setViews(4);
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsRepository.save(any(News.class))).thenAnswer(call -> call.getArgument(0));

        assertThat(newsService.view(1L).getViews()).isEqualTo(5);
    }

    private News news(String titleRu) {
        News news = new News();
        news.setTitleRu(titleRu);
        return news;
    }
}