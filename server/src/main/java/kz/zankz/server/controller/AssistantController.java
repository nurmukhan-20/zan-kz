package kz.zankz.server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.zankz.server.dto.AssistantRequest;
import kz.zankz.server.dto.AssistantResponse;
import kz.zankz.server.dto.CompareRequest;
import kz.zankz.server.dto.CompareResponse;
import kz.zankz.server.dto.DocumentRequest;
import kz.zankz.server.dto.DocumentResponse;
import kz.zankz.server.dto.QuizRequest;
import kz.zankz.server.dto.QuizResponse;
import kz.zankz.server.dto.SituationRequest;
import kz.zankz.server.dto.SituationResponse;
import kz.zankz.server.service.AssistantService;

// ИИ-инструменты (нужен вход в аккаунт):
// POST /api/assistant/ask      — вопрос помощнику
// POST /api/assistant/document — составить документ
// POST /api/assistant/quiz     — тест по статьям
// POST /api/assistant/compare  — сравнить две статьи
// POST /api/assistant/situation — разбор жизненной ситуации
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public AssistantResponse ask(@Valid @RequestBody AssistantRequest request) {
        return assistantService.ask(request.question(), request.lang());
    }

    @PostMapping("/document")
    public DocumentResponse document(@Valid @RequestBody DocumentRequest request) {
        return assistantService.document(request);
    }

    @PostMapping("/quiz")
    public QuizResponse quiz(@RequestBody QuizRequest request) {
        return assistantService.quiz(request.categoryId(), request.lang());
    }

    @PostMapping("/compare")
    public CompareResponse compare(@Valid @RequestBody CompareRequest request) {
        return assistantService.compare(request.articleIds(), request.lang());
    }

    @PostMapping("/situation")
    public SituationResponse situation(@Valid @RequestBody SituationRequest request) {
        return assistantService.situation(request.situation(), request.lang());
    }
}