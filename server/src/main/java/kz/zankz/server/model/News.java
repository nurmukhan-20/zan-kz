package kz.zankz.server.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Новость об изменении законодательства (лента «Новости»)
@Entity
@Table(name = "news")
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Заголовок на казахском обязателен")
    private String titleKk;

    @NotBlank(message = "Заголовок на русском обязателен")
    private String titleRu;

    private String titleEn;

    @NotBlank(message = "Текст на казахском обязателен")
    @Column(columnDefinition = "TEXT")
    private String textKk;

    @NotBlank(message = "Текст на русском обязателен")
    @Column(columnDefinition = "TEXT")
    private String textRu;

    @Column(columnDefinition = "TEXT")
    private String textEn;

    // закон, которого касается новость (необязательно)
    @ManyToOne
    @JoinColumn(name = "law_id")
    private Law law;

    // статья, которую можно открыть из новости (необязательно)
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    // ключ новости из NewsLoader (n1, n2…): по нему новость обновляется при запуске; у новостей от админа — пусто
    private String slug;

    // источник: ссылка и название (Kazinform, adilet.zan.kz…)
    private String sourceUrl;

    private String sourceName;

    // время публикации «ЧЧ:ММ» — чтобы новости одного дня шли по порядку
    private String publishedTime;

    // фото через запятую: /news/n3-1.jpg,/news/n3-2.jpg (файлы лежат в resources/static/news)
    @Column(columnDefinition = "TEXT")
    private String images;

    // видео: /news/n3-video.mp4 или полная ссылка
    private String videoUrl;

    @NotNull(message = "Дата новости обязательна")
    private LocalDate publishedAt;

    // сколько раз новость открывали — для вкладки «Популярные»
    private long views;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitleKk() { return titleKk; }
    public void setTitleKk(String titleKk) { this.titleKk = titleKk; }

    public String getTitleRu() { return titleRu; }
    public void setTitleRu(String titleRu) { this.titleRu = titleRu; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getTextKk() { return textKk; }
    public void setTextKk(String textKk) { this.textKk = textKk; }

    public String getTextRu() { return textRu; }
    public void setTextRu(String textRu) { this.textRu = textRu; }

    public String getTextEn() { return textEn; }
    public void setTextEn(String textEn) { this.textEn = textEn; }

    public Law getLaw() { return law; }
    public void setLaw(Law law) { this.law = law; }

    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getPublishedTime() { return publishedTime; }
    public void setPublishedTime(String publishedTime) { this.publishedTime = publishedTime; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public LocalDate getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }

    public long getViews() { return views; }
    public void setViews(long views) { this.views = views; }
}