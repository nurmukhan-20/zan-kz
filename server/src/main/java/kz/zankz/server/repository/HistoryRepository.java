package kz.zankz.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.HistoryEntry;

public interface HistoryRepository extends JpaRepository<HistoryEntry, Long> {

    List<HistoryEntry> findTop20ByUserIdOrderByViewedAtDesc(Long userId);

    Optional<HistoryEntry> findByUserIdAndArticleId(Long userId, Long articleId);

    void deleteByUserId(Long userId);
}