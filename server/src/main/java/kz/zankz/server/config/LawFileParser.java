package kz.zankz.server.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LawFileParser {

    private static final Pattern ARTICLE = Pattern.compile(
            "^===\\s*(\\S+)\\s*\\|\\s*(\\S*)\\s*(?:\\|\\s*(\\S*)\\s*)?(?:\\|\\s*(\\S*)\\s*)?$");
    private static final Pattern FIELD = Pattern.compile("^\\[([a-z]+-[a-z]+)\\]$");

    // chapter — глава, paragraph — параграф (оба необязательны):
    // «=== 434 | 2 | 25» = статья 434, раздел 2, глава 25
    // «=== 36 | 1 | 2 | 1» = статья 36, раздел 1, глава 2, параграф 1
    public record Entry(String number, String section, String chapter, String paragraph,
            Map<String, String> fields) {
        public String field(String name) {
            return fields.get(name);
        }
    }

    private LawFileParser() {
    }

    public static List<Entry> parse(InputStream input) throws IOException {
        List<Entry> entries = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        String number = null;
        String section = null;
        String chapter = null;
        String paragraph = null;
        Map<String, StringBuilder> fields = null;
        StringBuilder current = null;

        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                line = line.replace("﻿", "");
                first = false;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            Matcher article = ARTICLE.matcher(trimmed);
            if (article.matches()) {
                if (number != null) {
                    entries.add(toEntry(number, section, chapter, paragraph, fields));
                }
                number = article.group(1);
                section = article.group(2).isEmpty() ? null : article.group(2);
                chapter = emptyToNull(article.group(3));
                paragraph = emptyToNull(article.group(4));
                fields = new LinkedHashMap<>();
                current = null;
                continue;
            }
            Matcher field = FIELD.matcher(trimmed);
            if (field.matches() && fields != null) {
                current = new StringBuilder();
                fields.put(field.group(1), current);
                continue;
            }
            if (current != null) {
                if (current.length() > 0) {
                    current.append('\n');
                }
                current.append(trimmed);
            }
        }
        if (number != null) {
            entries.add(toEntry(number, section, chapter, paragraph, fields));
        }
        return entries;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static Entry toEntry(String number, String section, String chapter, String paragraph,
            Map<String, StringBuilder> fields) {
        Map<String, String> result = new LinkedHashMap<>();
        fields.forEach((name, text) -> result.put(name, text.toString()));
        return new Entry(number, section, chapter, paragraph, result);
    }
}