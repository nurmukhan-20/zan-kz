package kz.zankz.server.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import kz.zankz.server.config.LawFileParser.Entry;

// Проверка файлов законов в resources/data: каждый читается, статьи не повторяются,
// у каждой статьи есть текст и объяснение на трёх языках
class LawFileParserTest {

    private static final String[] FIELDS = {
            "text-kk", "text-ru", "text-en", "explanation-kk", "explanation-ru", "explanation-en"
    };

    private static final Map<String, Integer> EXPECTED = Map.of(
            "criminal-code", 19,
            "administrative-code", 28,
            "labor-code", 9,
            "family-code", 14,
            "tax-code", 15,
            "civil-code-general", 9,
            "civil-code-special", 10);

    private List<Entry> load(String name) throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/data/" + name + ".txt")) {
            assertThat(input).as("файл data/" + name + ".txt").isNotNull();
            return LawFileParser.parse(input);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "criminal-code", "administrative-code", "labor-code", "family-code",
            "tax-code", "civil-code-general", "civil-code-special" })
    @DisplayName("Файл закона читается полностью, все поля на трёх языках заполнены")
    void lawFileIsComplete(String name) throws Exception {
        List<Entry> entries = load(name);

        assertThat(entries).hasSize(EXPECTED.get(name));
        assertThat(entries).extracting(Entry::number).doesNotHaveDuplicates();
        for (Entry entry : entries) {
            assertThat(entry.section()).as(name + ", статья " + entry.number() + ": раздел").isNotBlank();
            for (String field : FIELDS) {
                assertThat(entry.field(field))
                        .as(name + ", статья " + entry.number() + ", поле " + field)
                        .isNotBlank();
            }
        }
    }

    @Test
    @DisplayName("Строка статьи: раздел, глава и параграф")
    void readsSectionChapterParagraph() throws Exception {
        String text = """
                # комментарий
                === 36 | 1 | 2 | 1
                [text-ru]
                Первая строка
                Вторая строка
                === 378 | 3:2 | 22
                [text-ru]
                Договор
                """;
        List<Entry> entries = LawFileParser.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).section()).isEqualTo("1");
        assertThat(entries.get(0).chapter()).isEqualTo("2");
        assertThat(entries.get(0).paragraph()).isEqualTo("1");
        assertThat(entries.get(0).field("text-ru")).isEqualTo("Первая строка\nВторая строка");
        assertThat(entries.get(1).section()).isEqualTo("3:2");
        assertThat(entries.get(1).chapter()).isEqualTo("22");
        assertThat(entries.get(1).paragraph()).isNull();
    }
}