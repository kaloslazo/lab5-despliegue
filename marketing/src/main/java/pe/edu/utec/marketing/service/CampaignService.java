package pe.edu.utec.marketing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utec.marketing.ai.ScriptGenerator;
import pe.edu.utec.marketing.ai.TemplateScriptGenerator;
import pe.edu.utec.marketing.media.ImageComposer;
import pe.edu.utec.marketing.media.Images;
import pe.edu.utec.marketing.media.MediaStorage;
import pe.edu.utec.marketing.media.VideoRenderer;
import pe.edu.utec.marketing.model.Asset;
import pe.edu.utec.marketing.model.Campaign;
import pe.edu.utec.marketing.model.CampaignStatus;
import pe.edu.utec.marketing.model.Network;
import pe.edu.utec.marketing.model.Publication;
import pe.edu.utec.marketing.model.Scene;
import pe.edu.utec.marketing.model.Script;
import pe.edu.utec.marketing.repository.CampaignRepository;
import pe.edu.utec.marketing.social.PublisherRegistry;
import pe.edu.utec.marketing.social.SocialPublisher;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    // 4:5 para el feed y 9:16 para el video
    static final int POST_W = 1080, POST_H = 1350;
    static final int VIDEO_W = 720, VIDEO_H = 1280;

    private final CampaignRepository campaigns;
    private final ScriptGenerator scriptGenerator;
    private final TemplateScriptGenerator fallbackGenerator;
    private final MediaStorage storage;
    private final ImageComposer composer;
    private final VideoRenderer videoRenderer;
    private final PublisherRegistry publishers;

    public CampaignService(CampaignRepository campaigns, ScriptGenerator scriptGenerator,
                           TemplateScriptGenerator fallbackGenerator, MediaStorage storage,
                           ImageComposer composer, VideoRenderer videoRenderer, PublisherRegistry publishers) {
        this.campaigns = campaigns;
        this.scriptGenerator = scriptGenerator;
        this.fallbackGenerator = fallbackGenerator;
        this.storage = storage;
        this.composer = composer;
        this.videoRenderer = videoRenderer;
        this.publishers = publishers;
    }

    // paso 1 y 2: recibe la foto y propone 3 guiones.
    // sin @Transactional para no tener una conexion abierta mientras responde el modelo
    public Campaign create(String businessName, String description, Network network,
                           String contentType, byte[] photo) {
        if (photo == null || photo.length == 0) {
            throw new CampaignException("Sube una foto");
        }
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new CampaignException("Solo se aceptan fotos JPG o PNG");
        }
        BufferedImage original = Images.fit(Images.read(photo), 2048);

        String folder = storage.newFolder();
        String photoPath = storage.save(folder, "original.jpg", Images.jpeg(original, 0.92f));
        Campaign campaign = new Campaign(businessName.strip(), description == null ? null : description.strip(),
                network, photoPath);

        byte[] forModel = Images.jpeg(Images.fit(original, 1568), 0.85f);
        List<Script> scripts;
        String source;
        try {
            scripts = scriptGenerator.generate(forModel, campaign.getBusinessName(), campaign.getDescription(), network);
            source = scriptGenerator.name();
        } catch (RuntimeException e) {
            log.warn("Fallo {} generando guiones, se usan plantillas: {}", scriptGenerator.name(), e.getMessage());
            scripts = fallbackGenerator.generate(forModel, campaign.getBusinessName(), campaign.getDescription(), network);
            source = fallbackGenerator.name();
        }
        campaign.addScripts(scripts, source);
        return campaigns.save(campaign);
    }

    // paso 3: se escoge uno de los guiones
    @Transactional
    public Campaign choose(Long campaignId, Long scriptId) {
        Campaign campaign = get(campaignId);
        if (campaign.getStatus() == CampaignStatus.PUBLISHED) {
            throw new CampaignException("La campaña ya fue publicada");
        }
        Script script = campaign.getScripts().stream()
                .filter(s -> s.getId().equals(scriptId))
                .findFirst()
                .orElseThrow(() -> new CampaignException("Ese guion no pertenece a la campaña"));
        campaign.choose(script);
        return campaign;
    }

    // paso 4a: genera 3 fotos y un video con el guion elegido
    @Transactional
    public Campaign generateMedia(Long campaignId) {
        Campaign campaign = get(campaignId);
        Script script = campaign.getChosenScript();
        if (script == null) {
            throw new CampaignException("Primero escoge un guion");
        }
        if (campaign.getStatus() == CampaignStatus.PUBLISHED) {
            throw new CampaignException("La campaña ya fue publicada");
        }

        BufferedImage original = Images.read(storage.read(campaign.getPhotoPath()));
        String folder = Path.of(campaign.getPhotoPath()).getParent().toString();
        String brand = campaign.getBusinessName();

        List<Asset> assets = new ArrayList<>();
        List<Path> videoFrames = new ArrayList<>();
        List<Scene> scenes = script.getScenes();
        for (int i = 0; i < scenes.size(); i++) {
            Scene scene = scenes.get(i);
            BufferedImage post = composer.compose(original, scene, i, brand, POST_W, POST_H);
            assets.add(new Asset(Asset.Kind.IMAGE, storage.save(folder, "post-" + (i + 1) + ".jpg", Images.jpeg(post, 0.9f))));

            BufferedImage frame = composer.compose(original, scene, i, brand, VIDEO_W, VIDEO_H, true);
            String framePath = storage.save(folder, "frame-" + (i + 1) + ".jpg", Images.jpeg(frame, 0.9f));
            videoFrames.add(storage.resolve(framePath));
        }

        String videoPath = folder + "/video.mp4";
        videoRenderer.render(videoFrames, storage.resolve(videoPath), VIDEO_W, VIDEO_H);
        assets.add(new Asset(Asset.Kind.VIDEO, videoPath));

        campaign.setAssets(assets);
        return campaign;
    }

    // paso 4b: publica en la red elegida
    @Transactional
    public Campaign publish(Long campaignId) {
        Campaign campaign = get(campaignId);
        if (campaign.getStatus() != CampaignStatus.MEDIA_READY) {
            throw new CampaignException(campaign.getStatus() == CampaignStatus.PUBLISHED
                    ? "La campaña ya fue publicada"
                    : "Primero genera las fotos y el video");
        }
        SocialPublisher publisher = publishers.forNetwork(campaign.getNetwork());
        Asset video = campaign.getVideo();
        SocialPublisher.PublishRequest request = new SocialPublisher.PublishRequest(
                campaign.getId(),
                campaign.getNetwork(),
                campaign.getChosenScript().fullCaption(),
                campaign.getImages().stream().map(Asset::getPath).toList(),
                video == null ? null : video.getPath());

        SocialPublisher.PublishResult result;
        try {
            result = publisher.publish(request);
        } catch (RuntimeException e) {
            log.error("Error publicando campaña {}", campaignId, e);
            throw new CampaignException("No se pudo publicar en " + campaign.getNetwork().getLabel() + ": " + e.getMessage());
        }
        campaign.addPublication(new Publication(campaign.getNetwork(), publisher.mode(), result.externalId(), result.url()));
        return campaign;
    }

    @Transactional(readOnly = true)
    public Campaign get(Long id) {
        return campaigns.findById(id).orElseThrow(() -> new NotFoundException("Campaña " + id + " no existe"));
    }

    @Transactional(readOnly = true)
    public List<Campaign> list() {
        return campaigns.findAllByOrderByCreatedAtDesc();
    }

    public String generatorName() {
        return scriptGenerator.name();
    }
}
