package kz.zankz.server.dto;

import java.util.List;

import kz.zankz.server.dto.AssistantResponse.ArticleRef;

// Тест: список вопросов. answer — номер правильного варианта (0..3), article — статья, по которой вопрос
public record QuizResponse(List<Question> questions) {

    public record Question(String question, List<String> options, int answer, String explanation, ArticleRef article) {
    }
}