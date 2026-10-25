package kz.zankz.server.dto;

// Запрос теста: categoryId — тема (пусто = все темы), lang — язык вопросов
public record QuizRequest(Long categoryId, String lang) {
}