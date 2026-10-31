package pe.edu.utec.champions.client;

import org.springframework.core.io.Resource;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

// se usa cuando no hay token de football-data (modo simulado)
public class SampleMatchSource implements MatchSource {

    private final Resource file;
    private final JsonMapper mapper;

    public SampleMatchSource(Resource file, JsonMapper mapper) {
        this.file = file;
        this.mapper = mapper;
    }

    @Override
    public List<ApiMatch> fetchMatches() {
        try (InputStream in = file.getInputStream()) {
            return mapper.readValue(in, ApiMatch.Response.class).matches();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + file.getFilename(), e);
        }
    }

    @Override
    public String name() {
        return "simulado";
    }
}
