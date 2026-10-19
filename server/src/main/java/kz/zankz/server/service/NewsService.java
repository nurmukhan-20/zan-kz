package kz.zankz.server.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.News;
import kz.zankz.server.repository.NewsRepository;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    // sort = latest (по дате) или popular (по просмотрам)
    public List<News> getAll(String sort) {
        if ("popular".equalsIgnoreCase(sort)) {
            return newsRepository.findAllByOrderByViewsDescPublishedAtDescPublishedTimeDesc();
        }
        return newsRepository.findAllByOrderByPublishedAtDescPublishedTimeDescIdDesc();
    }

    public News getById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Новость с номером " + id + " не найдена"));
    }

    // открыли новость — +1 просмотр
    @Transactional
    public News view(Long id) {
        News news = getById(id);
        news.setViews(news.getViews() + 1);
        return newsRepository.save(news);
    }

    public News create(News news) {
        news.setId(null);
        news.setViews(0);
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDate.now());
        }
        return newsRepository.save(news);
    }

    public void delete(Long id) {
        newsRepository.delete(getById(id));
    }
}