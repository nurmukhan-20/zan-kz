package kz.zankz.server.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.zankz.server.config.ConstitutionParser.ArticleData;

// Проверяем, что файл с текстами Конституции читается полностью и правильно
class ConstitutionParserTest {

    private static final String[] FIELDS = {
            "text-kk", "text-ru", "text-en", "explanation-kk", "explanation-ru", "explanation-en"
    };

    private List<ArticleData> load() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/data/constitution.txt")) {
            assertThat(input).as("файл data/constitution.txt").isNotNull();
            return ConstitutionParser.parse(input);
        }
    }

    @Test
    @DisplayName("В файле 41 статья: 11 в разделе I и 30 в разделе II")
    void fileHasAllArticles() throws Exception {
        List<ArticleData> articles = load();

        assertThat(articles).hasSize(41);
        assertThat(articles).filteredOn(a -> "I".equals(a.section())).hasSize(11);
        assertThat(articles).filteredOn(a -> "II".equals(a.section())).hasSize(30);
        assertThat(articles.get(0).number()).isEqualTo("1");
        assertThat(articles.get(40).number()).isEqualTo("41");
    }

    @Test
    @DisplayName("У каждой статьи заполнены тексты и объяснения на трёх языках")
    void everyArticleHasAllLanguages() throws Exception {
        for (ArticleData article : load()) {
            for (String field : FIELDS) {
                assertThat(article.field(field))
                        .as("статья " + article.number() + ", поле " + field)
                        .isNotBlank();
            }
        }
    }

    @Test
    @DisplayName("Пункты статьи сохраняются с переносами строк")
    void keepsLineBreaks() throws Exception {
        ArticleData article2 = load().get(1);

        assertThat(article2.field("text-ru"))
                .startsWith("1. Республика Казахстан – унитарное государство.")
                .contains("\n7. ");
    }
}