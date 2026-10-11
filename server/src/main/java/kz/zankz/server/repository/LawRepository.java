package kz.zankz.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Law;

public interface LawRepository extends JpaRepository<Law, Long> {

    // Все законы одной категории
    List<Law> findByCategoryId(Long categoryId);
}