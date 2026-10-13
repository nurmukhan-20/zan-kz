package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Term;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findAllByOrderByTermRuAsc();

    List<Term> findByTermKkContainingIgnoreCaseOrTermRuContainingIgnoreCaseOrTermEnContainingIgnoreCaseOrderByTermRuAsc(
            String kk, String ru, String en);
}