package kz.zankz.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.zankz.server.model.Term;
import kz.zankz.server.service.TermService;

@RestController
@RequestMapping("/api/terms")
public class TermController {

    private final TermService termService;

    public TermController(TermService termService) {
        this.termService = termService;
    }

    @GetMapping
    public List<Term> getAll(@RequestParam(required = false) String q) {
        return termService.getAll(q);
    }

    @GetMapping("/{id}")
    public Term getById(@PathVariable Long id) {
        return termService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Term create(@Valid @RequestBody Term term) {
        return termService.create(term);
    }

    @PutMapping("/{id}")
    public Term update(@PathVariable Long id, @Valid @RequestBody Term term) {
        return termService.update(id, term);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        termService.delete(id);
    }
}