package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kz.zankz.server.model.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Все статьи одного закона по порядку
    List<Article> findByLawIdOrderByIdAsc(Long lawId);

    // Статьи одного раздела закона, например раздел II Конституции («Мои права»)
    List<Article> findByLawIdAndSectionOrderByIdAsc(Long lawId, String section);

    // Поиск: точный номер статьи или слово в тексте/объяснении на любом из трёх языков
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