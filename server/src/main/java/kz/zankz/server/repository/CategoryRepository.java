package kz.zankz.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}