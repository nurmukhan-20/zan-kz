package kz.zankz.server.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kz.zankz.server.dto.AuthResponse;
import kz.zankz.server.dto.LoginRequest;
import kz.zankz.server.dto.RegisterRequest;
import kz.zankz.server.exception.BadRequestException;
import kz.zankz.server.model.Role;
import kz.zankz.server.model.User;
import kz.zankz.server.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Аккаунт с такой почтой уже существует");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setToken(UUID.randomUUID().toString());
        return toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Неверная почта или пароль"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Неверная почта или пароль");
        }
        user.setToken(UUID.randomUUID().toString());
        return toResponse(userRepository.save(user));
    }

    public AuthResponse toResponse(User user) {
        return new AuthResponse(user.getToken(), user.getName(), user.getEmail(), user.getRole().name());
    }
}