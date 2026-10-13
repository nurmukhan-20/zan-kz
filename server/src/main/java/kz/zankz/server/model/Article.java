package kz.zankz.server.model;

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

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Закон обязателен")
    @ManyToOne
    @JoinColumn(name = "law_id")
    private Law law;

    @NotBlank(message = "Номер статьи обязателен")
    private String articleNumber;

    private String section;

    private String titleKk;

    @NotBlank(message = "Название на русском обязательно")
    private String titleRu;

    private String titleEn;

    @Column(columnDefinition = "TEXT")
    private String textKk;

    @NotBlank(message = "Текст на русском обязателен")
    @Column(columnDefinition = "TEXT")
    private String textRu;

    @Column(columnDefinition = "TEXT")
    private String textEn;

    @Column(columnDefinition = "TEXT")
    private String explanationKk;

    @Column(columnDefinition = "TEXT")
    private String explanationRu;

    @Column(columnDefinition = "TEXT")
    private String explanationEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Law getLaw() { return law; }
    public void setLaw(Law law) { this.law = law; }

    public String getArticleNumber() { return articleNumber; }
    public void setArticleNumber(String articleNumber) { this.articleNumber = articleNumber; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

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

    public String getExplanationKk() { return explanationKk; }
    public void setExplanationKk(String explanationKk) { this.explanationKk = explanationKk; }

    public String getExplanationRu() { return explanationRu; }
    public void setExplanationRu(String explanationRu) { this.explanationRu = explanationRu; }

    public String getExplanationEn() { return explanationEn; }
    public void setExplanationEn(String explanationEn) { this.explanationEn = explanationEn; }
}