package kz.zankz.server.dto;

public record AuthResponse(String token, String name, String email, String role) {
}