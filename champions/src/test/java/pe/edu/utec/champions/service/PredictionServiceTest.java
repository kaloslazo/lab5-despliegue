package pe.edu.utec.champions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.utec.champions.model.Match;
import pe.edu.utec.champions.model.Outcome;
import pe.edu.utec.champions.model.Prediction;
import pe.edu.utec.champions.repository.MatchRepository;
import pe.edu.utec.champions.repository.PredictionRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PredictionServiceTest {

    private PredictionRepository predictions;
    private MatchRepository matches;
    private PredictionService service;

    @BeforeEach
    void setUp() {
        predictions = mock(PredictionRepository.class);
        matches = mock(MatchRepository.class);
        service = new PredictionService(predictions, matches);
        when(predictions.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Match match(long id, String status, Integer home, Integer away) {
        Match m = new Match(id, Instant.now(), status, "LEAGUE_STAGE", 4,
                "Real Madrid CF", "Liverpool FC", null, null, home, away);
        when(matches.findById(id)).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    void registraPrediccionValida() {
        match(1L, "TIMED", null, null);

        Prediction p = service.register(1L, "  Kalos ", Outcome.HOME, 2, 1);

        assertEquals("Kalos", p.getAuthor());
        assertEquals(Outcome.HOME, p.getOutcome());
        verify(predictions).save(any());
    }

    @Test
    void rechazaMarcadorQueNoCoincideConResultado() {
        match(1L, "TIMED", null, null);

        assertThrows(InvalidPredictionException.class,
                () -> service.register(1L, "Kalos", Outcome.DRAW, 2, 1));
        verify(predictions, never()).save(any());
    }

    @Test
    void rechazaPartidoFinalizado() {
        match(2L, "FINISHED", 1, 1);

        assertThrows(InvalidPredictionException.class,
                () -> service.register(2L, "Kalos", Outcome.DRAW, 1, 1));
    }

    @Test
    void rechazaGolesNegativos() {
        match(1L, "TIMED", null, null);

        assertThrows(InvalidPredictionException.class,
                () -> service.register(1L, "Kalos", Outcome.AWAY, -1, 0));
    }

    @Test
    void partidoInexistente() {
        when(matches.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.register(99L, "Kalos", Outcome.HOME, 1, 0));
    }

    @Test
    void aciertoSeCalculaCuandoTerminaElPartido() {
        Match m = new Match(3L, Instant.now(), "FINISHED", "LEAGUE_STAGE", 1,
                "A", "B", null, null, 2, 0);
        Prediction exacta = new Prediction(m, "x", Outcome.HOME, 2, 0);
        Prediction soloGanador = new Prediction(m, "y", Outcome.HOME, 1, 0);
        Prediction fallada = new Prediction(m, "z", Outcome.DRAW, 0, 0);

        assertEquals(true, exacta.getHit());
        assertEquals(true, exacta.isExactScore());
        assertEquals(true, soloGanador.getHit());
        assertEquals(false, soloGanador.isExactScore());
        assertEquals(false, fallada.getHit());
    }
}
