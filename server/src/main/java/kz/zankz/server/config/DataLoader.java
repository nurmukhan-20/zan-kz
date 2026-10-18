package kz.zankz.server.config;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import kz.zankz.server.config.LawFileParser.Entry;
import kz.zankz.server.model.Article;
import kz.zankz.server.model.Category;
import kz.zankz.server.model.Law;
import kz.zankz.server.repository.ArticleRepository;
import kz.zankz.server.repository.CategoryRepository;
import kz.zankz.server.repository.LawRepository;

@Component
public class DataLoader implements CommandLineRunner {

    record LawSource(String file, String categoryRu, String titleKk, String titleRu, String titleEn,
            String sourceUrl, LocalDate lastUpdated) {
    }

    static final List<LawSource> LAWS = List.of(
            new LawSource("/data/constitution.txt", "Конституция",
                    "Қазақстан Республикасының Конституциясы",
                    "Конституция Республики Казахстан",
                    "Constitution of the Republic of Kazakhstan",
                    "https://adilet.zan.kz/rus/docs/K2600000000",
                    LocalDate.of(2026, 10, 13)),
            new LawSource("/data/criminal-code.txt", "Уголовное право",
                    "Қазақстан Республикасының Қылмыстық кодексі",
                    "Уголовный кодекс Республики Казахстан",
                    "Criminal Code of the Republic of Kazakhstan",
                    "https://adilet.zan.kz/rus/docs/K1400000226",
                    LocalDate.of(2026, 7, 1)),
            new LawSource("/data/administrative-code.txt", "Административное право",
                    "Қазақстан Республикасының Әкімшілік құқық бұзушылық туралы кодексі",
                    "Кодекс Республики Казахстан об административных правонарушениях",
                    "Code of the Republic of Kazakhstan on Administrative Infractions",
                    "https://adilet.zan.kz/rus/docs/K1400000235",
                    LocalDate.of(2026, 7, 1)),
            new LawSource("/data/labor-code.txt", "Трудовое право",
                    "Қазақстан Республикасының Еңбек кодексі",
                    "Трудовой кодекс Республики Казахстан",
                    "Labor Code of the Republic of Kazakhstan",
                    "https://adilet.zan.kz/rus/docs/K1500000414",
                    LocalDate.of(2026, 6, 12)),
            new LawSource("/data/family-code.txt", "Семейное право",
                    "«Неке (ерлі-зайыптылық) және отбасы туралы» Қазақстан Республикасының Кодексі",
                    "Кодекс Республики Казахстан «О браке (супружестве) и семье»",
                    "Code of the Republic of Kazakhstan on Marriage (Matrimony) and Family",
                    "https://adilet.zan.kz/rus/docs/K1100000518",
                    LocalDate.of(2026, 7, 14)),
            new LawSource("/data/tax-code.txt", "Налоговое право",
                    "Қазақстан Республикасының Салық кодексі",
                    "Налоговый кодекс Республики Казахстан",
                    "Tax Code of the Republic of Kazakhstan",
                    "https://adilet.zan.kz/rus/docs/K2500000214",
                    LocalDate.of(2026, 7, 1)),
            // Гражданский кодекс на adilet — два документа: Общая и Особенная часть (оба в категории «Гражданское право»)
            new LawSource("/data/civil-code-general.txt", "Гражданское право",
                    "Қазақстан Республикасының Азаматтық кодексі (Жалпы бөлім)",
                    "Гражданский кодекс Республики Казахстан (Общая часть)",
                    "Civil Code of the Republic of Kazakhstan (General Part)",
                    "https://adilet.zan.kz/rus/docs/K940001000_",
                    LocalDate.of(2025, 6, 30)),
            new LawSource("/data/civil-code-special.txt", "Гражданское право",
                    "Қазақстан Республикасының Азаматтық кодексі (Ерекше бөлім)",
                    "Гражданский кодекс Республики Казахстан (Особенная часть)",
                    "Civil Code of the Republic of Kazakhstan (Special Part)",
                    "https://adilet.zan.kz/rus/docs/K990000409_",
                    LocalDate.of(2026, 5, 1)));

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
        if (categoryRepository.count() == 0) {
            createCategories();
        }
        removeUnusedCategories();
        for (LawSource source : LAWS) {
            Law law = saveLaw(source);
            syncArticles(law, source);
        }
    }

    private void createCategories() {
        category("Конституция", "Конституция", "Constitution", "book-outline");
        category("Қылмыстық құқық", "Уголовное право", "Criminal law", "shield-outline");
        category("Әкімшілік құқық", "Административное право", "Administrative law", "document-text-outline");
        category("Еңбек құқығы", "Трудовое право", "Labor law", "briefcase-outline");
        category("Отбасы құқығы", "Семейное право", "Family law", "people-outline");
        category("Салық құқығы", "Налоговое право", "Tax law", "cash-outline");
        category("Азаматтық құқық", "Гражданское право", "Civil law", "home-outline");
    }

    // категория «Права граждан» убрана: права граждан уже есть в «Моих правах» (Раздел II Конституции).
    // В базе, созданной раньше, она осталась — удаляем её при запуске (законов в ней нет).
    private static final List<String> REMOVED_CATEGORIES = List.of("Права граждан");

    private void removeUnusedCategories() {
        categoryRepository.findAll().stream()
                .filter(c -> REMOVED_CATEGORIES.contains(c.getNameRu()))
                .forEach(c -> {
                    categoryRepository.delete(c);
                    System.out.println("Удалена категория: " + c.getNameRu());
                });
    }

    private Law saveLaw(LawSource source) {
        Law law = lawRepository.findAll().stream()
                .filter(l -> source.sourceUrl().equals(l.getSourceUrl()))
                .findFirst()
                .orElseGet(Law::new);

        Category category = categoryRepository.findAll().stream()
                .filter(c -> source.categoryRu().equals(c.getNameRu()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Категория не найдена: " + source.categoryRu()));

        law.setTitleKk(source.titleKk());
        law.setTitleRu(source.titleRu());
        law.setTitleEn(source.titleEn());
        law.setCategory(category);
        law.setSourceUrl(source.sourceUrl());
        law.setLastUpdated(source.lastUpdated());
        return lawRepository.save(law);
    }

    private void syncArticles(Law law, LawSource source) throws Exception {
        List<Entry> fromFile;
        try (InputStream input = getClass().getResourceAsStream(source.file())) {
            if (input == null) {
                throw new IllegalStateException("Файл не найден: " + source.file());
            }
            fromFile = LawFileParser.parse(input);
        }

        Map<String, Article> existing = articleRepository.findAll().stream()
                .filter(a -> a.getLaw() != null && Objects.equals(a.getLaw().getId(), law.getId()))
                .collect(Collectors.toMap(Article::getArticleNumber, Function.identity(), (a, b) -> a));

        int added = 0;
        for (Entry entry : fromFile) {
            Article article = existing.get(entry.number());
            if (article == null) {
                article = new Article();
                added++;
            }
            fill(article, law, entry);
            articleRepository.save(article);
        }
        System.out.println(source.titleRu() + ": статей в файле " + fromFile.size() + ", добавлено новых " + added);
    }

    private Category category(String nameKk, String nameRu, String nameEn, String icon) {
        Category category = new Category();
        category.setNameKk(nameKk);
        category.setNameRu(nameRu);
        category.setNameEn(nameEn);
        category.setIcon(icon);
        return categoryRepository.save(category);
    }

    private void fill(Article article, Law law, Entry entry) {
        String number = entry.number();
        article.setLaw(law);
        article.setArticleNumber(number);
        // раздел, глава и параграф хранятся вместе: «2/25» = раздел 2, глава 25;
        // «1/2/1» = раздел 1, глава 2, параграф 1; «3:2/22» = раздел 3, подраздел 2, глава 22
        // (у Конституции и УК — только раздел)
        String section = entry.section();
        if (section != null && entry.chapter() != null) {
            section = section + "/" + entry.chapter();
            if (entry.paragraph() != null) {
                section = section + "/" + entry.paragraph();
            }
        }
        article.setSection(section);
        article.setTitleKk(title(number + "-бап", entry.field("title-kk")));
        article.setTitleRu(title("Статья " + number, entry.field("title-ru")));
        article.setTitleEn(title("Article " + number, entry.field("title-en")));
        article.setTextKk(entry.field("text-kk"));
        article.setTextRu(entry.field("text-ru"));
        article.setTextEn(entry.field("text-en"));
        article.setExplanationKk(entry.field("explanation-kk"));
        article.setExplanationRu(entry.field("explanation-ru"));
        article.setExplanationEn(entry.field("explanation-en"));
    }

    // в базе название статьи — до 255 символов; очень длинные названия (например, ст. 608 КоАП)
    // обрезаем по слову и ставим «…» — полный текст всё равно есть в самой статье
    private static final int TITLE_MAX = 255;

    private static String title(String prefix, String name) {
        String full = name == null || name.isBlank() ? prefix : prefix + ". " + name;
        if (full.length() <= TITLE_MAX) {
            return full;
        }
        String cut = full.substring(0, TITLE_MAX - 1);
        int space = cut.lastIndexOf(' ');
        if (space > TITLE_MAX / 2) {
            cut = cut.substring(0, space);
        }
        return cut.replaceAll("[,;:\\s]+$", "") + "…";
    }
}