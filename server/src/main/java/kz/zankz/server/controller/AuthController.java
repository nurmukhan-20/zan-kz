package kz.zankz.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kz.zankz.server.dto.AuthResponse;
import kz.zankz.server.dto.LoginRequest;
import kz.zankz.server.dto.RegisterRequest;
import kz.zankz.server.model.User;
import kz.zankz.server.service.AuthService;

// Адреса для регистрации и входа
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Кто я: возвращает данные вошедшего пользователя
    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal User user) {
        return authService.toResponse(user);
    }
}