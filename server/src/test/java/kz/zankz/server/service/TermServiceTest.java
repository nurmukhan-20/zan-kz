package kz.zankz.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kz.zankz.server.model.Term;
import kz.zankz.server.repository.TermRepository;

@ExtendWith(MockitoExtension.class)
class TermServiceTest {

    @Mock
    private TermRepository termRepository;

    @InjectMocks
    private TermService termService;

    @Test
    @DisplayName("Пустой поиск возвращает все термины")
    void blankQueryReturnsAll() {
        Term term = term("Конституция");
        when(termRepository.findAllByOrderByTermRuAsc()).thenReturn(List.of(term));

        assertThat(termService.getAll("   ")).containsExactly(term);
    }

    @Test
    @DisplayName("Поиск убирает лишние пробелы вокруг слова")
    void searchTrimsQuery() {
        Term term = term("Референдум");
        when(termRepository
                .findByTermKkContainingIgnoreCaseOrTermRuContainingIgnoreCaseOrTermEnContainingIgnoreCaseOrderByTermRuAsc(
                        "рефер", "рефер", "рефер"))
                .thenReturn(List.of(term));

        assertThat(termService.getAll("  рефер  ")).containsExactly(term);
    }

    private Term term(String nameRu) {
        Term term = new Term();
        term.setTermRu(nameRu);
        return term;
    }
}