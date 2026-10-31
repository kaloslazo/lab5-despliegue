package pe.edu.utec.marketing.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Publica con la Instagram Graph API (cuenta business/creator).
 * Instagram descarga las fotos y el video desde URLs publicas, por eso se necesita PUBLIC_BASE_URL.
 * Se publica un carrusel con las 3 fotos y un reel con el video.
 */
public class InstagramPublisher implements SocialPublisher {

    private static final Logger log = LoggerFactory.getLogger(InstagramPublisher.class);

    private final RestClient http;
    private final String userId;
    private final String token;
    private final String publicBaseUrl;

    public InstagramPublisher(RestClient.Builder builder, String graphUrl, String userId, String token,
                              String publicBaseUrl) {
        this.http = builder.baseUrl(graphUrl).build();
        this.userId = userId;
        this.token = token;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public PublishResult publish(PublishRequest request) {
        List<String> children = new ArrayList<>();
        for (String path : request.imagePaths()) {
            children.add(createContainer(form("image_url", mediaUrl(path), "is_carousel_item", "true")));
        }
        String carousel = createContainer(form(
                "media_type", "CAROUSEL",
                "children", String.join(",", children),
                "caption", request.caption()));
        String carouselId = publishContainer(carousel);

        if (request.videoPath() != null) {
            String reel = createContainer(form(
                    "media_type", "REELS",
                    "video_url", mediaUrl(request.videoPath()),
                    "caption", request.caption(),
                    "share_to_feed", "false"));
            waitUntilReady(reel);
            publishContainer(reel);
        }

        String permalink = permalink(carouselId);
        log.info("Publicado en Instagram: {}", permalink);
        return new PublishResult(carouselId, permalink);
    }

    @Override
    public String mode() {
        return "api";
    }

    private String createContainer(MultiValueMap<String, String> body) {
        return post("/" + userId + "/media", body).id();
    }

    private String publishContainer(String creationId) {
        return post("/" + userId + "/media_publish", form("creation_id", creationId)).id();
    }

    // los videos se procesan en segundo plano, hay que esperar a que el contenedor este listo
    private void waitUntilReady(String containerId) {
        for (int i = 0; i < 40; i++) {
            Status s = http.get()
                    .uri(uri -> uri.path("/" + containerId)
                            .queryParam("fields", "status_code")
                            .queryParam("access_token", token).build())
                    .retrieve()
                    .body(Status.class);
            String code = s == null ? null : s.status_code();
            if ("FINISHED".equals(code)) return;
            if ("ERROR".equals(code) || "EXPIRED".equals(code)) {
                throw new IllegalStateException("Instagram no pudo procesar el video (" + code + ")");
            }
            sleep(3000);
        }
        throw new IllegalStateException("Instagram demoro demasiado procesando el video");
    }

    private String permalink(String mediaId) {
        Media m = http.get()
                .uri(uri -> uri.path("/" + mediaId)
                        .queryParam("fields", "permalink")
                        .queryParam("access_token", token).build())
                .retrieve()
                .body(Media.class);
        return m == null ? null : m.permalink();
    }

    private Created post(String path, MultiValueMap<String, String> body) {
        body.add("access_token", token);
        Created created = http.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Created.class);
        if (created == null || created.id() == null) {
            throw new IllegalStateException("Respuesta vacia de Instagram en " + path);
        }
        return created;
    }

    private String mediaUrl(String relative) {
        return publicBaseUrl + "/media/" + relative;
    }

    private static MultiValueMap<String, String> form(String... kv) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.add(kv[i], kv[i + 1]);
        }
        return map;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Created(String id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Status(String status_code) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Media(String permalink) {
    }
}
