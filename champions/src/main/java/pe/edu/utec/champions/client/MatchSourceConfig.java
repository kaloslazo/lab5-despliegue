package pe.edu.utec.champions.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class MatchSourceConfig {

    @Bean
    MatchSource matchSource(RestClient.Builder builder, JsonMapper mapper,
                            @Value("${football-data.base-url}") String baseUrl,
                            @Value("${football-data.token:}") String token,
                            @Value("${football-data.competition}") String competition,
                            @Value("classpath:data/cl-matches.json") Resource sample) {
        if (token == null || token.isBlank()) {
            return new SampleMatchSource(sample, mapper);
        }
        return new FootballDataClient(builder, baseUrl, token, competition);
    }
}
