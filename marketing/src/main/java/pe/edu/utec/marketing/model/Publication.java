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

import java.time.Instant;

@Entity
@Table(name = "publications")
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Network network;

    @Column(nullable = false, length = 20)
    private String mode;

    private String externalId;
    private String url;

    @Column(nullable = false)
    private Instant publishedAt;

    protected Publication() {
    }

    public Publication(Network network, String mode, String externalId, String url) {
        this.network = network;
        this.mode = mode;
        this.externalId = externalId;
        this.url = url;
        this.publishedAt = Instant.now();
    }

    void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Long getId() { return id; }
    public Network getNetwork() { return network; }
    public String getMode() { return mode; }
    public String getExternalId() { return externalId; }
    public String getUrl() { return url; }
    public Instant getPublishedAt() { return publishedAt; }
}
