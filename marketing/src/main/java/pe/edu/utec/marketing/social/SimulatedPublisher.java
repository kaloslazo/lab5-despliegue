package pe.edu.utec.marketing.social;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

// no llama a ninguna red, solo deja el registro y una vista previa del post dentro de la app
public class SimulatedPublisher implements SocialPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPublisher.class);

    private final String publicBaseUrl;

    public SimulatedPublisher(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public PublishResult publish(PublishRequest request) {
        String id = "sim-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Publicacion simulada {} en {} ({} fotos + video)", id, request.network(),
                request.imagePaths().size());
        return new PublishResult(id, publicBaseUrl + "/campaigns/" + request.campaignId() + "/preview");
    }

    @Override
    public String mode() {
        return "simulado";
    }
}
