package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Все статьи одного закона по порядку добавления
    List<Article> findByLawIdOrderByIdAsc(Long lawId);
}