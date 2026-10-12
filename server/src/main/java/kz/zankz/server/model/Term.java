package kz.zankz.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

// Юридический термин с простым объяснением на трёх языках
@Entity
@Table(name = "terms")
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Термин на казахском обязателен")
    private String termKk;

    @NotBlank(message = "Термин на русском обязателен")
    private String termRu;

    private String termEn;

    @NotBlank(message = "Объяснение на казахском обязательно")
    @Column(columnDefinition = "TEXT")
    private String definitionKk;

    @NotBlank(message = "Объяснение на русском обязательно")
    @Column(columnDefinition = "TEXT")
    private String definitionRu;

    @Column(columnDefinition = "TEXT")
    private String definitionEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTermKk() { return termKk; }
    public void setTermKk(String termKk) { this.termKk = termKk; }

    public String getTermRu() { return termRu; }
    public void setTermRu(String termRu) { this.termRu = termRu; }

    public String getTermEn() { return termEn; }
    public void setTermEn(String termEn) { this.termEn = termEn; }

    public String getDefinitionKk() { return definitionKk; }
    public void setDefinitionKk(String definitionKk) { this.definitionKk = definitionKk; }

    public String getDefinitionRu() { return definitionRu; }
    public void setDefinitionRu(String definitionRu) { this.definitionRu = definitionRu; }

    public String getDefinitionEn() { return definitionEn; }
    public void setDefinitionEn(String definitionEn) { this.definitionEn = definitionEn; }
}