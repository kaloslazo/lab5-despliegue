package pe.edu.utec.marketing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "assets")
public class Asset {

    public enum Kind { IMAGE, VIDEO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Kind kind;

    // ruta relativa dentro del directorio de media
    @Column(nullable = false)
    private String path;

    protected Asset() {
    }

    public Asset(Kind kind, String path) {
        this.kind = kind;
        this.path = path;
    }

    void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public boolean isImage() {
        return kind == Kind.IMAGE;
    }

    public Long getId() { return id; }
    public Kind getKind() { return kind; }
    public String getPath() { return path; }
}
