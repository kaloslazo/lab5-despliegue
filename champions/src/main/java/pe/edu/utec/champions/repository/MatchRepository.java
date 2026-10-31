package pe.edu.utec.champions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utec.champions.model.Match;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findAllByOrderByUtcDateAsc();

    List<Match> findByStageOrderByUtcDateAsc(String stage);
}
