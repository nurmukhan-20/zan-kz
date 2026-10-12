package kz.zankz.server.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import kz.zankz.server.event.LawUpdatedEvent;
import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Category;
import kz.zankz.server.model.Law;
import kz.zankz.server.repository.LawRepository;

// Вся логика работы с законами
@Service
public class LawService {

    private final LawRepository lawRepository;
    private final CategoryService categoryService;
    private final ApplicationEventPublisher eventPublisher;

    public LawService(LawRepository lawRepository, CategoryService categoryService,
            ApplicationEventPublisher eventPublisher) {
        this.lawRepository = lawRepository;
        this.categoryService = categoryService;
        this.eventPublisher = eventPublisher;
    }

    // Все законы или только законы одной категории
    public List<Law> getAll(Long categoryId) {
        if (categoryId != null) {
            return lawRepository.findByCategoryId(categoryId);
        }
        return lawRepository.findAll();
    }

    public Law getById(Long id) {
        return lawRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Закон с номером " + id + " не найден"));
    }

    public Law create(Law law) {
        law.setId(null); // номер назначает база данных
        law.setCategory(findCategory(law));
        return lawRepository.save(law);
    }

    public Law update(Long id, Law data) {
        Law law = getById(id);
        law.setTitleKk(data.getTitleKk());
        law.setTitleRu(data.getTitleRu());
        law.setTitleEn(data.getTitleEn());
        law.setSourceUrl(data.getSourceUrl());
        law.setLastUpdated(data.getLastUpdated());
        law.setCategory(findCategory(data));
        Law saved = lawRepository.save(law);
        // сообщаем, что закон изменился (на это отреагирует LawUpdatedListener)
        eventPublisher.publishEvent(new LawUpdatedEvent(saved));
        return saved;
    }

    public void delete(Long id) {
        Law law = getById(id);
        lawRepository.delete(law);
    }

    // Проверяем, что указанная категория действительно есть в базе
    private Category findCategory(Law law) {
        if (law.getCategory() == null || law.getCategory().getId() == null) {
            throw new ResourceNotFoundException("Не указан номер категории");
        }
        return categoryService.getById(law.getCategory().getId());
    }
}