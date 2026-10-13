package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kz.zankz.server.model.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByLawIdOrderByIdAsc(Long lawId);

    List<Article> findByLawIdAndSectionOrderByIdAsc(Long lawId, String section);

    @Query("""
            SELECT a FROM Article a
            WHERE a.articleNumber = :q
               OR LOWER(a.textKk) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.textRu) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.textEn) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.explanationKk) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.explanationRu) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.explanationEn) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY a.id
            """)
    List<Article> search(@Param("q") String q);
}