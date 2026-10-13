package kz.zankz.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название на казахском обязательно")
    private String nameKk;

    @NotBlank(message = "Название на русском обязательно")
    private String nameRu;

    private String nameEn;

    private String icon;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNameKk() { return nameKk; }
    public void setNameKk(String nameKk) { this.nameKk = nameKk; }

    public String getNameRu() { return nameRu; }
    public void setNameRu(String nameRu) { this.nameRu = nameRu; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}