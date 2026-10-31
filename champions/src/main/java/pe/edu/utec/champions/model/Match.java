package pe.edu.utec.champions.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "matches")
public class Match {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    // se usa el id de la API externa para no duplicar partidos al recargar
    @Id
    private Long id;

    @Column(nullable = false)
    private Instant utcDate;

    private String status;
    private String stage;
    private Integer matchday;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    private String homeCrest;
    private String awayCrest;
    private Integer homeScore;
    private Integer awayScore;

    protected Match() {
    }

    public Match(Long id, Instant utcDate, String status, String stage, Integer matchday,
                 String homeTeam, String awayTeam, String homeCrest, String awayCrest,
                 Integer homeScore, Integer awayScore) {
        this.id = id;
        this.utcDate = utcDate;
        this.status = status;
        this.stage = stage;
        this.matchday = matchday;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeCrest = homeCrest;
        this.awayCrest = awayCrest;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public boolean isFinished() {
        return "FINISHED".equals(status) && homeScore != null && awayScore != null;
    }

    public Outcome getOutcome() {
        if (!isFinished()) {
            return null;
        }
        return Outcome.of(homeScore, awayScore);
    }

    public ZonedDateTime getKickoffLima() {
        return utcDate.atZone(LIMA);
    }

    public Long getId() { return id; }
    public Instant getUtcDate() { return utcDate; }
    public String getStatus() { return status; }
    public String getStage() { return stage; }
    public Integer getMatchday() { return matchday; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public String getHomeCrest() { return homeCrest; }
    public String getAwayCrest() { return awayCrest; }
    public Integer getHomeScore() { return homeScore; }
    public Integer getAwayScore() { return awayScore; }
}
