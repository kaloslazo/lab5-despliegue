package pe.edu.utec.marketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utec.marketing.model.Script;

public interface ScriptRepository extends JpaRepository<Script, Long> {
}
