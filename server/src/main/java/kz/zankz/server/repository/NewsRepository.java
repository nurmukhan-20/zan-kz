package kz.zankz.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.News;

public interface NewsRepository extends JpaRepository<News, Long> {

    // «Последние»: сначала самые новые
    List<News> findAllByOrderByPublishedAtDescPublishedTimeDescIdDesc();

    // «Популярные»: сначала самые просматриваемые
    List<News> findAllByOrderByViewsDescPublishedAtDescPublishedTimeDesc();

    Optional<News> findFirstByTitleRu(String titleRu);

    Optional<News> findFirstBySlug(String slug);
}