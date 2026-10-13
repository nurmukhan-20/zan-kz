package kz.zankz.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import kz.zankz.server.event.LawUpdatedEvent;
import kz.zankz.server.model.Category;
import kz.zankz.server.model.Law;
import kz.zankz.server.repository.LawRepository;

@ExtendWith(MockitoExtension.class)
class LawServiceTest {

    @Mock
    private LawRepository lawRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LawService lawService;

    @Test
    @DisplayName("При изменении закона публикуется событие LawUpdatedEvent")
    void updatePublishesEvent() {
        Category category = category();
        Law existing = new Law();
        existing.setId(1L);
        existing.setCategory(category);
        when(lawRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryService.getById(1L)).thenReturn(category);
        when(lawRepository.save(any(Law.class))).thenAnswer(call -> call.getArgument(0));

        lawService.update(1L, data(category));

        ArgumentCaptor<LawUpdatedEvent> event = ArgumentCaptor.forClass(LawUpdatedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().law().getTitleRu()).isEqualTo("Конституция Республики Казахстан");
    }

    @Test
    @DisplayName("При создании закона событие не публикуется")
    void createDoesNotPublishEvent() {
        Category category = category();
        when(categoryService.getById(1L)).thenReturn(category);
        when(lawRepository.save(any(Law.class))).thenAnswer(call -> call.getArgument(0));

        lawService.create(data(category));

        verifyNoInteractions(eventPublisher);
    }

    private Category category() {
        Category category = new Category();
        category.setId(1L);
        return category;
    }

    private Law data(Category category) {
        Law law = new Law();
        law.setTitleKk("Қазақстан Республикасының Конституциясы");
        law.setTitleRu("Конституция Республики Казахстан");
        law.setCategory(category);
        law.setSourceUrl("https://adilet.zan.kz/rus/docs/K2600000000");
        law.setLastUpdated(LocalDate.of(2026, 10, 12));
        return law;
    }
}