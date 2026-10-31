package pe.edu.utec.champions.model;

public enum Outcome {
    HOME("Gana local"),
    DRAW("Empate"),
    AWAY("Gana visita");

    private final String label;

    Outcome(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Outcome of(int homeGoals, int awayGoals) {
        if (homeGoals > awayGoals) return HOME;
        if (homeGoals < awayGoals) return AWAY;
        return DRAW;
    }
}
