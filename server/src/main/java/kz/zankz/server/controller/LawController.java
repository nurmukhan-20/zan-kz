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
import kz.zankz.server.model.Law;
import kz.zankz.server.service.LawService;

@RestController
@RequestMapping("/api/laws")
public class LawController {

    private final LawService lawService;

    public LawController(LawService lawService) {
        this.lawService = lawService;
    }

    @GetMapping
    public List<Law> getAll(@RequestParam(required = false) Long categoryId) {
        return lawService.getAll(categoryId);
    }

    @GetMapping("/{id}")
    public Law getById(@PathVariable Long id) {
        return lawService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Law create(@Valid @RequestBody Law law) {
        return lawService.create(law);
    }

    @PutMapping("/{id}")
    public Law update(@PathVariable Long id, @Valid @RequestBody Law law) {
        return lawService.update(id, law);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        lawService.delete(id);
    }
}