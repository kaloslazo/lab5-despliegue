package pe.edu.utec.marketing.social;

import pe.edu.utec.marketing.model.Network;

import java.util.List;

public interface SocialPublisher {

    record PublishRequest(Long campaignId, Network network, String caption,
                          List<String> imagePaths, String videoPath) {
    }

    record PublishResult(String externalId, String url) {
    }

    PublishResult publish(PublishRequest request);

    // "simulado" o "api"
    String mode();
}
