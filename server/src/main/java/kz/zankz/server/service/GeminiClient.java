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

// Связь с Gemini API. Ключ берётся из local.properties (app.gemini.key) и никуда не отправляется, кроме Google.
// Если Gemini перегружен (503) или временно сбоит (500/502/504) — пробуем ещё 2 раза с паузой.
@Service
public class GeminiClient {

    private static final int MAX_ATTEMPTS = 3;                     // 1 попытка + 2 повтора
    private static final long[] PAUSE_MS = { 0, 1500, 4000 };      // пауза перед каждой попыткой

    private final RestClient http;
    private final String apiKey;
    private final String model;

    public GeminiClient(@Value("${app.gemini.key:}") String apiKey,
            @Value("${app.gemini.model:gemini-3.6-flash}") String model) {
        this.apiKey = apiKey.trim();
        this.model = model.trim();
        HttpClient jdk = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofSeconds(40));
        this.http = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    // instruction — правила для ИИ, question — текст пользователя. Возвращает ответ Gemini.
    public String ask(String instruction, String question) {
        if (!isConfigured()) {
            throw new BadRequestException("ИИ-помощник не настроен: нет ключа app.gemini.key в local.properties");
        }

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", instruction))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", question)))),
                "generationConfig", Map.of("temperature", 0.2));

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            pause(PAUSE_MS[attempt]);
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
                    throw new BadRequestException("ИИ не дал ответа. Попробуйте переформулировать вопрос.");
                }
                return text;
            } catch (RestClientResponseException e) {
                int code = e.getStatusCode().value();
                boolean temporary = code == 500 || code == 502 || code == 503 || code == 504;
                if (temporary && attempt < MAX_ATTEMPTS - 1) {
                    continue;   // Gemini занят — пробуем ещё раз
                }
                throw new BadRequestException(messageFor(code));
            } catch (RestClientException e) {
                if (attempt < MAX_ATTEMPTS - 1) continue;   // сеть мигнула — ещё попытка
                throw new BadRequestException("Нет связи с Gemini. Проверьте интернет на ноутбуке.");
            }
        }
        throw new BadRequestException(messageFor(503));
    }

    private String messageFor(int code) {
        return switch (code) {
            case 429 -> "ИИ-помощник сейчас перегружен (лимит бесплатного тарифа). Подождите минуту и попробуйте снова.";
            case 503, 500, 502, 504 -> "Gemini сейчас перегружен на стороне Google. Подождите немного и попробуйте снова.";
            case 404 -> "Модель «" + model + "» не найдена. Проверьте app.gemini.model в local.properties.";
            case 400, 401, 403 -> "Gemini не принял запрос. Проверьте ключ app.gemini.key в local.properties.";
            default -> "Ошибка Gemini, код " + code + ". Попробуйте позже.";
        };
    }

    private static void pause(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ответ Gemini: candidates[0].content.parts[].text (части с "thought": true — это «мысли» модели, пропускаем)
    private static String extractText(Map<?, ?> response) {
        if (response == null) return "";
        Object candidates = response.get("candidates");
        if (!(candidates instanceof List<?> list) || list.isEmpty()) return "";
        if (!(list.get(0) instanceof Map<?, ?> first)) return "";
        if (!(first.get("content") instanceof Map<?, ?> content)) return "";
        if (!(content.get("parts") instanceof List<?> parts)) return "";
        StringBuilder sb = new StringBuilder();
        for (Object p : parts) {
            if (p instanceof Map<?, ?> part && !Boolean.TRUE.equals(part.get("thought"))
                    && part.get("text") instanceof String text) {
                sb.append(text);
            }
        }
        return sb.toString().trim();
    }
}