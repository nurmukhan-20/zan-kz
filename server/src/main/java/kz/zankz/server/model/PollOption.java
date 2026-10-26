package kz.zankz.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Вариант ответа: «Да», «Нет» или партия в бюллетене. position — порядок, как в бюллетене
@Entity
@Table(name = "voting_options")
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "voting_id", nullable = false)
    private Poll poll;

    private int position;

    @Column(length = 300) private String textKk;
    @Column(length = 300) private String textRu;
    @Column(length = 300) private String textEn;

    public PollOption() {
    }

    public PollOption(String textKk, String textRu, String textEn) {
        this.textKk = textKk;
        this.textRu = textRu;
        this.textEn = textEn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getTextKk() { return textKk; }
    public void setTextKk(String textKk) { this.textKk = textKk; }
    public String getTextRu() { return textRu; }
    public void setTextRu(String textRu) { this.textRu = textRu; }
    public String getTextEn() { return textEn; }
    public void setTextEn(String textEn) { this.textEn = textEn; }
}