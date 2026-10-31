package pe.edu.utec.champions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// requerimiento 2: al iniciar se guardan todos los partidos en la BD
@Component
public class StartupLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupLoader.class);

    private final MatchService matchService;

    public StartupLoader(MatchService matchService) {
        this.matchService = matchService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            matchService.syncMatches();
        } catch (Exception e) {
            // si la API falla la app igual levanta con lo que ya habia en la BD
            log.error("No se pudo cargar partidos desde {}: {}", matchService.sourceName(), e.getMessage());
        }
    }
}
