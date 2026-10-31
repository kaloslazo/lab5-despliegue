package pe.edu.utec.marketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utec.marketing.model.Campaign;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findAllByOrderByCreatedAtDesc();
}
