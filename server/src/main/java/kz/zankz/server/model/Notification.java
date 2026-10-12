package kz.zankz.server.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

// Уведомление об изменениях в законодательстве
@Entity
@Table(name = "notifications")
public class Notification {

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
    private String messageKk;

    @NotBlank(message = "Текст на русском обязателен")
    @Column(columnDefinition = "TEXT")
    private String messageRu;

    @Column(columnDefinition = "TEXT")
    private String messageEn;

    // Какого закона касается уведомление (может быть пустым)
    @ManyToOne
    @JoinColumn(name = "law_id")
    private Law law;

    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitleKk() { return titleKk; }
    public void setTitleKk(String titleKk) { this.titleKk = titleKk; }

    public String getTitleRu() { return titleRu; }
    public void setTitleRu(String titleRu) { this.titleRu = titleRu; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getMessageKk() { return messageKk; }
    public void setMessageKk(String messageKk) { this.messageKk = messageKk; }

    public String getMessageRu() { return messageRu; }
    public void setMessageRu(String messageRu) { this.messageRu = messageRu; }

    public String getMessageEn() { return messageEn; }
    public void setMessageEn(String messageEn) { this.messageEn = messageEn; }

    public Law getLaw() { return law; }
    public void setLaw(Law law) { this.law = law; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}