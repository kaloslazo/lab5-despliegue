package pe.edu.utec.champions.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

// mismo formato que devuelve /v4/competitions/CL/matches de football-data.org
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiMatch(Long id, Instant utcDate, String status, String stage, Integer matchday,
                       Team homeTeam, Team awayTeam, Score score) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Team(Long id, String name, String shortName, String crest) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Score(String winner, Goals fullTime) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Goals(Integer home, Integer away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<ApiMatch> matches) {
    }
}
