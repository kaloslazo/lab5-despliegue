package pe.edu.utec.marketing.model;

public enum Network {
    INSTAGRAM("Instagram"),
    TIKTOK("TikTok");

    private final String label;

    Network(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
