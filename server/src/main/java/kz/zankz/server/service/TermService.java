package kz.zankz.server.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kz.zankz.server.exception.ResourceNotFoundException;
import kz.zankz.server.model.Term;
import kz.zankz.server.repository.TermRepository;

@Service
public class TermService {

    private final TermRepository termRepository;

    public TermService(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    public List<Term> getAll(String query) {
        if (query == null || query.isBlank()) {
            return termRepository.findAllByOrderByTermRuAsc();
        }
        String q = query.trim();
        return termRepository
                .findByTermKkContainingIgnoreCaseOrTermRuContainingIgnoreCaseOrTermEnContainingIgnoreCaseOrderByTermRuAsc(q, q, q);
    }

    public Term getById(Long id) {
        return termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Термин с номером " + id + " не найден"));
    }

    public Term create(Term term) {
        term.setId(null);
        return termRepository.save(term);
    }

    public Term update(Long id, Term data) {
        Term term = getById(id);
        term.setTermKk(data.getTermKk());
        term.setTermRu(data.getTermRu());
        term.setTermEn(data.getTermEn());
        term.setDefinitionKk(data.getDefinitionKk());
        term.setDefinitionRu(data.getDefinitionRu());
        term.setDefinitionEn(data.getDefinitionEn());
        return termRepository.save(term);
    }

    public void delete(Long id) {
        termRepository.delete(getById(id));
    }
}