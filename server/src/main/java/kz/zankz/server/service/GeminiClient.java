package kz.zankz.server.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import kz.zankz.server.exception.BadRequestException;

// Связь с Gemini (ИИ от Google).
// Ключ и модель берутся из local.properties: app.gemini.key и app.gemini.model.
// Ключ хранится только на сервере — в приложение он никогда не попадает.
@Service
public class GeminiClient {

    private final RestClient http;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            @Value("${app.gemini.key:}") String apiKey,
            @Value("${app.gemini.model:gemini-3.6-flash}") String model) {
        this.apiKey = apiKey.trim();
        this.model = model.trim();

        // ждём подключения не больше 10 секунд, ответа — не больше 40 секунд
        HttpClient jdk = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofSeconds(40));

        this.http = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    // true — ключ вписан в local.properties
    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    // Отправить ИИ инструкцию (как себя вести) и вопрос. Возвращает текст ответа.
    public String ask(String instruction, String question) {
        if (!isConfigured()) {
            throw new BadRequestException("ИИ-помощник не настроен: на сервере нет ключа app.gemini.key");
        }

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", instruction))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", question)))),
                // 0.2 — отвечать точно и без фантазий
                "generationConfig", Map.of("temperature", 0.2));

        try {
            Map<?, ?> response = http.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            String text = extractText(response);
            if (text.isBlank()) {
                throw new BadRequestException("ИИ не смог ответить на этот вопрос. Попробуйте сформулировать иначе.");
            }
            return text;
        } catch (RestClientResponseException e) {
            int code = e.getStatusCode().value();
            if (code == 429) {
                throw new BadRequestException("ИИ-помощник сейчас перегружен (лимит бесплатного тарифа). Попробуйте через минуту.");
            }
            if (code == 404) {
                throw new BadRequestException("Модель «" + model + "» не найдена. Проверьте app.gemini.model в local.properties.");
            }
            if (code == 400 || code == 401 || code == 403) {
                throw new BadRequestException("Gemini не принял запрос: проверьте ключ app.gemini.key (код " + code + ").");
            }
            throw new BadRequestException("Ошибка Gemini, код " + code + ". Попробуйте позже.");
        } catch (RestClientException e) {
            throw new BadRequestException("Нет связи с Gemini. Проверьте интернет на ноутбуке.");
        }
    }

    // Достаём текст из ответа: candidates[0].content.parts[].text
    private static String extractText(Map<?, ?> response) {
        if (response == null || !(response.get("candidates") instanceof List<?> candidates) || candidates.isEmpty()) {
            return "";
        }
        if (!(candidates.get(0) instanceof Map<?, ?> first) || !(first.get("content") instanceof Map<?, ?> content)) {
            return "";
        }
        if (!(content.get("parts") instanceof List<?> parts)) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (Object part : parts) {
            // части с "thought": true — это «размышления» модели, их не показываем
            if (part instanceof Map<?, ?> p && p.get("text") instanceof String t && !Boolean.TRUE.equals(p.get("thought"))) {
                text.append(t);
            }
        }
        return text.toString().trim();
    }
}