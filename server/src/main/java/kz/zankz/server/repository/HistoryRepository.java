package kz.zankz.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kz.zankz.server.model.HistoryEntry;

public interface HistoryRepository extends JpaRepository<HistoryEntry, Long> {

    List<HistoryEntry> findTop20ByUserIdOrderByViewedAtDesc(Long userId);

    Optional<HistoryEntry> findByUserIdAndArticleId(Long userId, Long articleId);

    void deleteByUserId(Long userId);

    // популярные статьи: номер статьи и сколько разных пользователей её открывали
    @Query("SELECT h.article.id, COUNT(h) FROM HistoryEntry h GROUP BY h.article.id ORDER BY COUNT(h) DESC")
    List<Object[]> countViewsByArticle(Pageable pageable);
}