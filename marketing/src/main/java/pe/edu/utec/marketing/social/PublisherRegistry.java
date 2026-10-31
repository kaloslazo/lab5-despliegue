package pe.edu.utec.marketing.social;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.edu.utec.marketing.model.Network;

import java.util.EnumMap;
import java.util.Map;

// decide que publicador usar por red segun las credenciales configuradas
@Component
public class PublisherRegistry {

    private final Map<Network, SocialPublisher> publishers = new EnumMap<>(Network.class);

    public PublisherRegistry(RestClient.Builder builder,
                             @Value("${app.public-base-url}") String publicBaseUrl,
                             @Value("${instagram.graph-url}") String graphUrl,
                             @Value("${instagram.user-id:}") String igUserId,
                             @Value("${instagram.token:}") String igToken) {
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        SocialPublisher simulated = new SimulatedPublisher(base);

        if (!igUserId.isBlank() && !igToken.isBlank()) {
            publishers.put(Network.INSTAGRAM, new InstagramPublisher(builder, graphUrl, igUserId, igToken, base));
        } else {
            publishers.put(Network.INSTAGRAM, simulated);
        }
        // la API de TikTok exige app auditada para publicar, por ahora queda simulado
        publishers.put(Network.TIKTOK, simulated);
    }

    public SocialPublisher forNetwork(Network network) {
        return publishers.get(network);
    }
}
