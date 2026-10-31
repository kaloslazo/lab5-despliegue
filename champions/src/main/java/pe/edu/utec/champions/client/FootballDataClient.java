package pe.edu.utec.champions.client;

import org.springframework.web.client.RestClient;

import java.util.List;

public class FootballDataClient implements MatchSource {

    private final RestClient restClient;
    private final String competition;

    public FootballDataClient(RestClient.Builder builder, String baseUrl, String token, String competition) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Auth-Token", token)
                .build();
        this.competition = competition;
    }

    @Override
    public List<ApiMatch> fetchMatches() {
        ApiMatch.Response response = restClient.get()
                .uri("/competitions/{code}/matches", competition)
                .retrieve()
                .body(ApiMatch.Response.class);
        return response == null || response.matches() == null ? List.of() : response.matches();
    }

    @Override
    public String name() {
        return "football-data.org";
    }
}
