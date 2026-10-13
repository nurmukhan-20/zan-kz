package kz.zankz.server.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "laws")
public class Law {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название на казахском обязательно")
    private String titleKk;

    @NotBlank(message = "Название на русском обязательно")
    private String titleRu;

    private String titleEn;

    @NotNull(message = "Категория обязательна")
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @NotBlank(message = "Официальный источник обязателен")
    private String sourceUrl;

    @NotNull(message = "Дата обновления обязательна")
    private LocalDate lastUpdated;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitleKk() { return titleKk; }
    public void setTitleKk(String titleKk) { this.titleKk = titleKk; }

    public String getTitleRu() { return titleRu; }
    public void setTitleRu(String titleRu) { this.titleRu = titleRu; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }
}