package pe.edu.utec.champions.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.utec.champions.model.Outcome;

public class PredictionForm {

    @NotBlank(message = "Ingresa tu nombre")
    @Size(max = 60)
    private String author;

    @NotNull(message = "Elige un resultado")
    private Outcome outcome;

    @NotNull(message = "Requerido")
    @Min(0) @Max(20)
    private Integer homeGoals;

    @NotNull(message = "Requerido")
    @Min(0) @Max(20)
    private Integer awayGoals;

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }
    public Integer getHomeGoals() { return homeGoals; }
    public void setHomeGoals(Integer homeGoals) { this.homeGoals = homeGoals; }
    public Integer getAwayGoals() { return awayGoals; }
    public void setAwayGoals(Integer awayGoals) { this.awayGoals = awayGoals; }
}
