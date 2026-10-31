package pe.edu.utec.champions.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utec.champions.model.Prediction;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    @EntityGraph(attributePaths = "match")
    List<Prediction> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "match")
    List<Prediction> findByMatchIdOrderByCreatedAtDesc(Long matchId);
}
