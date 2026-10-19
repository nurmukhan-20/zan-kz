package kz.zankz.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.zankz.server.model.News;
import kz.zankz.server.service.NewsService;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    // GET /api/news?sort=latest  или  /api/news?sort=popular
    @GetMapping
    public List<News> getAll(@RequestParam(defaultValue = "latest") String sort) {
        return newsService.getAll(sort);
    }

    // открытие новости считается просмотром
    @GetMapping("/{id}")
    public News getById(@PathVariable Long id) {
        return newsService.view(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public News create(@Valid @RequestBody News news) {
        return newsService.create(news);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        newsService.delete(id);
    }
}