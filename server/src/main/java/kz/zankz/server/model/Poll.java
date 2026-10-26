package kz.zankz.server.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

// Голосование (как бюллетень): вид, название, дата, о чём оно, вопрос и варианты ответа. Всё на трёх языках.
@Entity
@Table(name = "votings")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String kind;               // REFERENDUM — референдум, ELECTION — выборы, PROPOSAL — предложение

    private LocalDate eventDate;       // дата голосования

    @Column(length = 300) private String titleKk;
    @Column(length = 300) private String titleRu;
    @Column(length = 300) private String titleEn;

    @Column(columnDefinition = "TEXT") private String descriptionKk;   // о чём голосование, простыми словами
    @Column(columnDefinition = "TEXT") private String descriptionRu;
    @Column(columnDefinition = "TEXT") private String descriptionEn;

    @Column(length = 500) private String questionKk;                  // вопрос как в бюллетене
    @Column(length = 500) private String questionRu;
    @Column(length = 500) private String questionEn;

    private boolean active = true;     // false — голосование закрыто, видны только результаты

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PollOption> options = new ArrayList<>();

    public void addOption(PollOption option) {
        option.setPoll(this);
        option.setPosition(options.size());
        options.add(option);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getTitleKk() { return titleKk; }
    public void setTitleKk(String titleKk) { this.titleKk = titleKk; }
    public String getTitleRu() { return titleRu; }
    public void setTitleRu(String titleRu) { this.titleRu = titleRu; }
    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getDescriptionKk() { return descriptionKk; }
    public void setDescriptionKk(String descriptionKk) { this.descriptionKk = descriptionKk; }
    public String getDescriptionRu() { return descriptionRu; }
    public void setDescriptionRu(String descriptionRu) { this.descriptionRu = descriptionRu; }
    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }

    public String getQuestionKk() { return questionKk; }
    public void setQuestionKk(String questionKk) { this.questionKk = questionKk; }
    public String getQuestionRu() { return questionRu; }
    public void setQuestionRu(String questionRu) { this.questionRu = questionRu; }
    public String getQuestionEn() { return questionEn; }
    public void setQuestionEn(String questionEn) { this.questionEn = questionEn; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<PollOption> getOptions() { return options; }
}