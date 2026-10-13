package kz.zankz.server.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Читает статьи из текстового файла data/constitution.txt.
// Строка "=== 12 | II" начинает статью 12 из раздела II,
// строка "[text-ru]" начинает поле, строки с "#" — комментарии.
public class ConstitutionParser {

    // Одна статья из файла
    public record ArticleData(String number, String section, Map<String, String> fields) {

        public String field(String name) {
            return fields.get(name);
        }
    }

    public static List<ArticleData> parse(InputStream input) {
        List<ArticleData> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String number = null;
            String section = null;
            Map<String, String> fields = null;
            String field = null;
            StringBuilder value = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("=== ")) {
                    if (number != null) {
                        save(fields, field, value);
                        result.add(new ArticleData(number, section, fields));
                    }
                    String[] parts = line.substring(4).split("\\|");
                    number = parts[0].trim();
                    section = parts.length > 1 ? parts[1].trim() : null;
                    fields = new LinkedHashMap<>();
                    field = null;
                    value.setLength(0);
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]") && number != null) {
                    save(fields, field, value);
                    field = line.substring(1, line.length() - 1);
                    value.setLength(0);
                    continue;
                }
                if (field != null) {
                    if (value.length() > 0) {
                        value.append('\n');
                    }
                    value.append(line);
                }
            }
            if (number != null) {
                save(fields, field, value);
                result.add(new ArticleData(number, section, fields));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }

    private static void save(Map<String, String> fields, String field, StringBuilder value) {
        if (fields != null && field != null) {
            String text = value.toString().strip();
            fields.put(field, text.isEmpty() ? null : text);
        }
    }
}