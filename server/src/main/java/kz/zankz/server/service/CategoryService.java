package kz.zankz.server.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Category;
import kz.zankz.server.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория с номером " + id + " не найдена"));
    }

    public Category create(Category category) {
        category.setId(null);
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category data) {
        Category category = getById(id);
        category.setNameKk(data.getNameKk());
        category.setNameRu(data.getNameRu());
        category.setNameEn(data.getNameEn());
        category.setIcon(data.getIcon());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }
}