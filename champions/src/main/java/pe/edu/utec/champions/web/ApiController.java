package pe.edu.utec.champions.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utec.champions.model.Match;
import pe.edu.utec.champions.model.Prediction;
import pe.edu.utec.champions.service.MatchService;
import pe.edu.utec.champions.service.PredictionService;

import java.time.Instant;
import java.util.List;

// endpoints json, utiles para probar con curl/postman
@RestController
@RequestMapping("/api")
public class ApiController {

    private final MatchService matchService;
    private final PredictionService predictionService;

    public ApiController(MatchService matchService, PredictionService predictionService) {
        this.matchService = matchService;
        this.predictionService = predictionService;
    }

    @GetMapping("/matches")
    public List<MatchDto> matches(@RequestParam(required = false) String stage) {
        return matchService.list(stage).stream().map(MatchDto::from).toList();
    }

    @GetMapping("/predictions")
    public List<PredictionDto> predictions() {
        return predictionService.listAll().stream().map(PredictionDto::from).toList();
    }

    public record MatchDto(Long id, Instant utcDate, String status, String stage,
                           String homeTeam, String awayTeam, Integer homeScore, Integer awayScore) {
        static MatchDto from(Match m) {
            return new MatchDto(m.getId(), m.getUtcDate(), m.getStatus(), m.getStage(),
                    m.getHomeTeam(), m.getAwayTeam(), m.getHomeScore(), m.getAwayScore());
        }
    }

    public record PredictionDto(Long id, Long matchId, String match, String author, String outcome,
                                int homeGoals, int awayGoals, Boolean hit, Instant createdAt) {
        static PredictionDto from(Prediction p) {
            Match m = p.getMatch();
            return new PredictionDto(p.getId(), m.getId(), m.getHomeTeam() + " vs " + m.getAwayTeam(),
                    p.getAuthor(), p.getOutcome().name(), p.getHomeGoals(), p.getAwayGoals(),
                    p.getHit(), p.getCreatedAt());
        }
    }
}
