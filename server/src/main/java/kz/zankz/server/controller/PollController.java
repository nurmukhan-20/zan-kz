package kz.zankz.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.zankz.server.dto.PollRequest;
import kz.zankz.server.dto.PollResponse;
import kz.zankz.server.dto.VoteRequest;
import kz.zankz.server.model.User;
import kz.zankz.server.service.PollService;

// Голосование:
// GET    /api/polls            — список с результатами (вошедший пользователь)
// POST   /api/polls/{id}/vote  — проголосовать { "optionId": 5 } (один раз)
// POST   /api/polls            — создать (ADMIN)
// POST   /api/polls/{id}/close — закрыть (ADMIN)
// DELETE /api/polls/{id}       — удалить (ADMIN)
@RestController
@RequestMapping("/api/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    @GetMapping
    public List<PollResponse> getAll(@AuthenticationPrincipal User user) {
        return pollService.getAll(user);
    }

    @PostMapping("/{id}/vote")
    public PollResponse vote(@AuthenticationPrincipal User user, @PathVariable Long id,
            @Valid @RequestBody VoteRequest request) {
        return pollService.vote(user, id, request.optionId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PollResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody PollRequest request) {
        return pollService.create(request, user);
    }

    @PostMapping("/{id}/close")
    public PollResponse close(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return pollService.close(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        pollService.delete(id);
    }
}