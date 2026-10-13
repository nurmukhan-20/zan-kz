package kz.zankz.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import kz.zankz.server.dto.AuthResponse;
import kz.zankz.server.dto.LoginRequest;
import kz.zankz.server.dto.RegisterRequest;
import kz.zankz.server.exception.BadRequestException;
import kz.zankz.server.model.Role;
import kz.zankz.server.model.User;
import kz.zankz.server.repository.UserRepository;

// Тесты регистрации и входа
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Регистрация: роль USER, почта в нижнем регистре, пароль зашифрован")
    void registerCreatesUser() {
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        AuthResponse response = authService.register(new RegisterRequest(" Test ", " Test@Mail.com ", "123456"));

        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.email()).isEqualTo("test@mail.com");
        assertThat(response.token()).isNotBlank();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("123456");
        assertThat(passwordEncoder.matches("123456", saved.getValue().getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Повторная регистрация на ту же почту запрещена")
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Test", "test@mail.com", "123456")))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Вход с неверным паролем запрещён")
    void loginRejectsWrongPassword() {
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user("123456")));

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@mail.com", "wrong-password")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Вход с неизвестной почтой запрещён")
    void loginRejectsUnknownEmail() {
        when(userRepository.findByEmail("nobody@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@mail.com", "123456")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Успешный вход выдаёт новый ключ")
    void loginGivesNewToken() {
        User user = user("123456");
        user.setToken("old-token");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        AuthResponse response = authService.login(new LoginRequest("test@mail.com", "123456"));

        assertThat(response.token()).isNotEqualTo("old-token");
        assertThat(response.role()).isEqualTo("USER");
    }

    private User user(String password) {
        User user = new User();
        user.setId(1L);
        user.setName("Test");
        user.setEmail("test@mail.com");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        return user;
    }
}