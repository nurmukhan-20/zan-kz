package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Term;

public interface TermRepository extends JpaRepository<Term, Long> {

    // Все термины по алфавиту
    List<Term> findAllByOrderByTermRuAsc();

    // Поиск по слову на любом из трёх языков
    List<Term> findByTermKkContainingIgnoreCaseOrTermRuContainingIgnoreCaseOrTermEnContainingIgnoreCaseOrderByTermRuAsc(
            String kk, String ru, String en);
}