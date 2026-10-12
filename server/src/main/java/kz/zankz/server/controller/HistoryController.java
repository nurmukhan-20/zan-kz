package kz.zankz.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kz.zankz.server.dto.HistoryResponse;
import kz.zankz.server.model.User;
import kz.zankz.server.service.HistoryService;

// Адреса для истории просмотров (только после входа)
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<HistoryResponse> getRecent(@AuthenticationPrincipal User user) {
        return historyService.getRecent(user);
    }

    @PostMapping("/{articleId}")
    public HistoryResponse record(@AuthenticationPrincipal User user, @PathVariable Long articleId) {
        return historyService.record(user, articleId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal User user) {
        historyService.clear(user);
    }
}