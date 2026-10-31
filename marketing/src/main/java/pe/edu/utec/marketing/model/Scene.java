package pe.edu.utec.marketing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Scene {

    @Column(length = 140)
    private String overlay;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Look look;

    protected Scene() {
    }

    public Scene(String overlay, Look look) {
        this.overlay = overlay;
        this.look = look;
    }

    public String getOverlay() { return overlay; }
    public Look getLook() { return look; }
}
