package kz.zankz.server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.zankz.server.dto.AssistantRequest;
import kz.zankz.server.dto.AssistantResponse;
import kz.zankz.server.service.AssistantService;

// POST /api/assistant/ask — вопрос ИИ-помощнику (нужен вход в аккаунт)
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
}