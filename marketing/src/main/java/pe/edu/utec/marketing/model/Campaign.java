package pe.edu.utec.marketing.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String businessName;

    @Column(length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Network network;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status;

    @Column(nullable = false)
    private String photoPath;

    // de donde salieron los guiones: claude o plantillas
    @Column(length = 40)
    private String scriptSource;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Script> scripts = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "chosen_script_id")
    private Script chosenScript;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Asset> assets = new ArrayList<>();

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("publishedAt")
    private List<Publication> publications = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    protected Campaign() {
    }

    public Campaign(String businessName, String description, Network network, String photoPath) {
        this.businessName = businessName;
        this.description = description;
        this.network = network;
        this.photoPath = photoPath;
        this.status = CampaignStatus.SCRIPTS_READY;
        this.createdAt = Instant.now();
    }

    public void addScripts(List<Script> newScripts, String source) {
        newScripts.forEach(s -> {
            s.setCampaign(this);
            scripts.add(s);
        });
        this.scriptSource = source;
    }

    public void choose(Script script) {
        this.chosenScript = script;
        this.status = CampaignStatus.SCRIPT_CHOSEN;
        this.assets.clear();
    }

    public void setAssets(List<Asset> generated) {
        assets.clear();
        generated.forEach(a -> {
            a.setCampaign(this);
            assets.add(a);
        });
        this.status = CampaignStatus.MEDIA_READY;
    }

    public void addPublication(Publication publication) {
        publication.setCampaign(this);
        publications.add(publication);
        this.status = CampaignStatus.PUBLISHED;
    }

    public List<Asset> getImages() {
        return assets.stream().filter(Asset::isImage).toList();
    }

    public Asset getVideo() {
        return assets.stream().filter(a -> !a.isImage()).findFirst().orElse(null);
    }

    public ZonedDateTime getCreatedAtLima() {
        return createdAt.atZone(ZoneId.of("America/Lima"));
    }

    public Long getId() { return id; }
    public String getBusinessName() { return businessName; }
    public String getDescription() { return description; }
    public Network getNetwork() { return network; }
    public CampaignStatus getStatus() { return status; }
    public String getPhotoPath() { return photoPath; }
    public String getScriptSource() { return scriptSource; }
    public List<Script> getScripts() { return scripts; }
    public Script getChosenScript() { return chosenScript; }
    public List<Asset> getAssets() { return assets; }
    public List<Publication> getPublications() { return publications; }
    public Instant getCreatedAt() { return createdAt; }
}
