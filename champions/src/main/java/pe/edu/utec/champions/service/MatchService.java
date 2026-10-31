package pe.edu.utec.champions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utec.champions.client.ApiMatch;
import pe.edu.utec.champions.client.MatchSource;
import pe.edu.utec.champions.model.Match;
import pe.edu.utec.champions.repository.MatchRepository;

import java.util.List;
import java.util.Objects;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchSource source;
    private final MatchRepository matches;

    public MatchService(MatchSource source, MatchRepository matches) {
        this.source = source;
        this.matches = matches;
    }

    @Transactional
    public int syncMatches() {
        List<Match> fetched = source.fetchMatches().stream()
                .filter(m -> m.homeTeam() != null && m.homeTeam().name() != null)
                .filter(m -> m.awayTeam() != null && m.awayTeam().name() != null)
                .map(MatchService::toEntity)
                .toList();
        matches.saveAll(fetched);
        log.info("{} partidos sincronizados desde {}", fetched.size(), source.name());
        return fetched.size();
    }

    @Transactional(readOnly = true)
    public List<Match> list(String stage) {
        if (stage == null || stage.isBlank()) {
            return matches.findAllByOrderByUtcDateAsc();
        }
        return matches.findByStageOrderByUtcDateAsc(stage);
    }

    @Transactional(readOnly = true)
    public List<String> stages() {
        return matches.findAllByOrderByUtcDateAsc().stream()
                .map(Match::getStage)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public Match get(Long id) {
        return matches.findById(id)
                .orElseThrow(() -> new NotFoundException("Partido " + id + " no existe"));
    }

    public String sourceName() {
        return source.name();
    }

    static Match toEntity(ApiMatch m) {
        Integer home = null;
        Integer away = null;
        if (m.score() != null && m.score().fullTime() != null) {
            home = m.score().fullTime().home();
            away = m.score().fullTime().away();
        }
        return new Match(m.id(), m.utcDate(), m.status(), m.stage(), m.matchday(),
                m.homeTeam().name(), m.awayTeam().name(),
                m.homeTeam().crest(), m.awayTeam().crest(),
                home, away);
    }
}
