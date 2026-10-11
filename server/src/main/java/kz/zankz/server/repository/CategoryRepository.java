package kz.zankz.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kz.zankz.server.model.Category;

// Готовые команды для таблицы категорий: найти, сохранить, удалить
public interface CategoryRepository extends JpaRepository<Category, Long> {
}