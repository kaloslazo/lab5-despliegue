package pe.edu.utec.champions.client;

import java.util.List;

public interface MatchSource {

    List<ApiMatch> fetchMatches();

    String name();
}
