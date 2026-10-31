package pe.edu.utec.marketing.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scripts")
public class Script {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 200)
    private String angle;

    @Column(nullable = false, length = 200)
    private String hook;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "script_scenes", joinColumns = @JoinColumn(name = "script_id"))
    @OrderColumn(name = "position")
    private List<Scene> scenes = new ArrayList<>();

    @Column(nullable = false, length = 2200)
    private String caption;

    @Column(length = 500)
    private String hashtags;

    @Column(length = 120)
    private String callToAction;

    protected Script() {
    }

    public Script(String title, String angle, String hook, List<Scene> scenes,
                  String caption, String hashtags, String callToAction) {
        this.title = title;
        this.angle = angle;
        this.hook = hook;
        this.scenes = new ArrayList<>(scenes);
        this.caption = caption;
        this.hashtags = hashtags;
        this.callToAction = callToAction;
    }

    void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public String fullCaption() {
        StringBuilder sb = new StringBuilder(caption);
        if (callToAction != null && !callToAction.isBlank()) {
            sb.append("\n\n").append(callToAction);
        }
        if (hashtags != null && !hashtags.isBlank()) {
            sb.append("\n\n").append(hashtags);
        }
        return sb.toString();
    }

    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public String getTitle() { return title; }
    public String getAngle() { return angle; }
    public String getHook() { return hook; }
    public List<Scene> getScenes() { return scenes; }
    public String getCaption() { return caption; }
    public String getHashtags() { return hashtags; }
    public String getCallToAction() { return callToAction; }
}
