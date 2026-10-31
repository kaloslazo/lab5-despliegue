package pe.edu.utec.champions.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utec.champions.model.Match;
import pe.edu.utec.champions.model.Outcome;
import pe.edu.utec.champions.model.Prediction;
import pe.edu.utec.champions.repository.MatchRepository;
import pe.edu.utec.champions.repository.PredictionRepository;

import java.util.List;
import java.util.Set;

@Service
public class PredictionService {

    private static final Set<String> CLOSED = Set.of("FINISHED", "IN_PLAY", "PAUSED", "AWARDED", "CANCELLED");
    private static final int MAX_GOALS = 20;

    private final PredictionRepository predictions;
    private final MatchRepository matches;

    public PredictionService(PredictionRepository predictions, MatchRepository matches) {
        this.predictions = predictions;
        this.matches = matches;
    }

    @Transactional
    public Prediction register(Long matchId, String author, Outcome outcome, int homeGoals, int awayGoals) {
        Match match = matches.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partido " + matchId + " no existe"));

        if (CLOSED.contains(match.getStatus())) {
            throw new InvalidPredictionException("El partido ya empezo o termino, no se aceptan predicciones");
        }
        if (homeGoals < 0 || awayGoals < 0 || homeGoals > MAX_GOALS || awayGoals > MAX_GOALS) {
            throw new InvalidPredictionException("El marcador debe estar entre 0 y " + MAX_GOALS);
        }
        if (Outcome.of(homeGoals, awayGoals) != outcome) {
            throw new InvalidPredictionException("El marcador " + homeGoals + "-" + awayGoals
                    + " no coincide con el resultado elegido (" + outcome.getLabel() + ")");
        }

        return predictions.save(new Prediction(match, author.trim(), outcome, homeGoals, awayGoals));
    }

    @Transactional(readOnly = true)
    public List<Prediction> listAll() {
        return predictions.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Prediction> listByMatch(Long matchId) {
        return predictions.findByMatchIdOrderByCreatedAtDesc(matchId);
    }
}
