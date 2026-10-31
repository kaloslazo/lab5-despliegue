package pe.edu.utec.champions.model;

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
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(nullable = false, length = 60)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Outcome outcome;

    @Column(nullable = false)
    private int homeGoals;

    @Column(nullable = false)
    private int awayGoals;

    @Column(nullable = false)
    private Instant createdAt;

    protected Prediction() {
    }

    public Prediction(Match match, String author, Outcome outcome, int homeGoals, int awayGoals) {
        this.match = match;
        this.author = author;
        this.outcome = outcome;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.createdAt = Instant.now();
    }

    // null mientras el partido no termina
    public Boolean getHit() {
        Outcome real = match.getOutcome();
        return real == null ? null : real == outcome;
    }

    public boolean isExactScore() {
        return match.isFinished()
                && match.getHomeScore() == homeGoals
                && match.getAwayScore() == awayGoals;
    }

    public ZonedDateTime getCreatedAtLima() {
        return createdAt.atZone(ZoneId.of("America/Lima"));
    }

    public Long getId() { return id; }
    public Match getMatch() { return match; }
    public String getAuthor() { return author; }
    public Outcome getOutcome() { return outcome; }
    public int getHomeGoals() { return homeGoals; }
    public int getAwayGoals() { return awayGoals; }
    public Instant getCreatedAt() { return createdAt; }
}
