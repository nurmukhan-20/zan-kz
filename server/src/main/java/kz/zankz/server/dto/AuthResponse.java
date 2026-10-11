package kz.zankz.server.dto;

// Ответ сервера после входа или регистрации. Пароль сюда не попадает
public record AuthResponse(String token, String name, String email, String role) {
}