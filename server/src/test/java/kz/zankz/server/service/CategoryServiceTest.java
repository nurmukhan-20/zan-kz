package kz.zankz.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Category;
import kz.zankz.server.repository.CategoryRepository;

// Тесты сервиса категорий. Вместо настоящей базы — заглушка (mock)
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Находит категорию по номеру")
    void getByIdReturnsCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setNameRu("Конституция");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThat(categoryService.getById(1L).getNameRu()).isEqualTo("Конституция");
    }

    @Test
    @DisplayName("Если категории нет — ошибка «не найдена»")
    void getByIdThrowsWhenMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("При создании номер из запроса игнорируется — его назначает база")
    void createIgnoresIdFromRequest() {
        Category category = new Category();
        category.setId(555L);
        category.setNameRu("Тест");
        when(categoryRepository.save(any(Category.class))).thenAnswer(call -> call.getArgument(0));

        Category saved = categoryService.create(category);

        assertThat(saved.getId()).isNull();
        verify(categoryRepository).save(category);
    }
}