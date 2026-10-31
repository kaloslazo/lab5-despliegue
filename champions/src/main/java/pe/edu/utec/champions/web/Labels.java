package pe.edu.utec.champions.web;

import org.springframework.stereotype.Component;

import java.util.Map;

// textos en español para los codigos que devuelve la API
@Component("labels")
public class Labels {

    private static final Map<String, String> STAGES = Map.of(
            "LEAGUE_STAGE", "Fase liga",
            "PLAYOFFS", "Playoffs",
            "LAST_16", "Octavos",
            "QUARTER_FINALS", "Cuartos",
            "SEMI_FINALS", "Semifinal",
            "FINAL", "Final");

    private static final Map<String, String> STATUS = Map.of(
            "SCHEDULED", "Programado",
            "TIMED", "Programado",
            "IN_PLAY", "En juego",
            "PAUSED", "Entretiempo",
            "FINISHED", "Finalizado",
            "POSTPONED", "Postergado",
            "CANCELLED", "Cancelado");

    public String stage(String code) {
        return code == null ? "-" : STAGES.getOrDefault(code, code);
    }

    public String status(String code) {
        return code == null ? "-" : STATUS.getOrDefault(code, code);
    }
}
