package kz.zankz.server.config;

import java.io.InputStream;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import kz.zankz.server.config.ConstitutionParser.ArticleData;
import kz.zankz.server.model.Article;
import kz.zankz.server.model.Category;
import kz.zankz.server.model.Law;
import kz.zankz.server.repository.ArticleRepository;
import kz.zankz.server.repository.CategoryRepository;
import kz.zankz.server.repository.LawRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final LawRepository lawRepository;
    private final ArticleRepository articleRepository;

    public DataLoader(CategoryRepository categoryRepository, LawRepository lawRepository,
            ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.lawRepository = lawRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category constitution = category("Конституция", "Конституция", "Constitution", "book-outline");
        category("Қылмыстық құқық", "Уголовное право", "Criminal law", "shield-outline");
        category("Әкімшілік құқық", "Административное право", "Administrative law", "document-text-outline");
        category("Еңбек құқығы", "Трудовое право", "Labor law", "briefcase-outline");
        category("Отбасы құқығы", "Семейное право", "Family law", "people-outline");
        category("Салық құқығы", "Налоговое право", "Tax law", "cash-outline");
        category("Азаматтық құқық", "Гражданское право", "Civil law", "home-outline");
        category("Азаматтардың құқықтары", "Права граждан", "Citizens' rights", "person-outline");

        Law law = new Law();
        law.setTitleKk("Қазақстан Республикасының Конституциясы");
        law.setTitleRu("Конституция Республики Казахстан");
        law.setTitleEn("Constitution of the Republic of Kazakhstan");
        law.setCategory(constitution);
        law.setSourceUrl("https://adilet.zan.kz/rus/docs/K2600000000");
        law.setLastUpdated(LocalDate.of(2026, 10, 13));
        law = lawRepository.save(law);

        try (InputStream input = getClass().getResourceAsStream("/data/constitution.txt")) {
            if (input == null) {
                throw new IllegalStateException("Файл data/constitution.txt не найден");
            }
            for (ArticleData data : ConstitutionParser.parse(input)) {
                articleRepository.save(article(law, data));
            }
        }
    }

    private Category category(String nameKk, String nameRu, String nameEn, String icon) {
        Category category = new Category();
        category.setNameKk(nameKk);
        category.setNameRu(nameRu);
        category.setNameEn(nameEn);
        category.setIcon(icon);
        return categoryRepository.save(category);
    }

    private Article article(Law law, ArticleData data) {
        String number = data.number();
        Article article = new Article();
        article.setLaw(law);
        article.setArticleNumber(number);
        article.setSection(data.section());
        article.setTitleKk(number + "-бап");
        article.setTitleRu("Статья " + number);
        article.setTitleEn("Article " + number);
        article.setTextKk(data.field("text-kk"));
        article.setTextRu(data.field("text-ru"));
        article.setTextEn(data.field("text-en"));
        article.setExplanationKk(data.field("explanation-kk"));
        article.setExplanationRu(data.field("explanation-ru"));
        article.setExplanationEn(data.field("explanation-en"));
        return article;
    }
}