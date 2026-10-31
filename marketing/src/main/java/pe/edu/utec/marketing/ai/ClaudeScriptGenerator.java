package pe.edu.utec.marketing.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredOutputConfig;
import pe.edu.utec.marketing.model.Network;
import pe.edu.utec.marketing.model.Script;

import java.util.Base64;
import java.util.List;

public class ClaudeScriptGenerator implements ScriptGenerator {

    private static final String SYSTEM = """
            Eres redactor creativo de una agencia de marketing digital en Peru.
            Recibes la foto de un producto o negocio y escribes guiones para publicaciones en redes sociales.
            Escribe en español latinoamericano, cercano y natural. Nada de promesas falsas ni datos inventados
            (precios, descuentos o premios) que no esten en la descripcion.
            Cada guion debe tener un enfoque distinto a los otros dos.
            Los textos de las escenas se imprimen encima de la foto, asi que deben ser cortos y legibles.
            """;

    private final AnthropicClient client;
    private final String model;

    public ClaudeScriptGenerator(AnthropicClient client, String model) {
        this.client = client;
        this.model = model;
    }

    @Override
    public List<Script> generate(byte[] jpeg, String businessName, String description, Network network) {
        String prompt = """
                Negocio: %s
                Descripcion que dio el cliente: %s
                Red social destino: %s

                Mira la foto y propone 3 guiones. Cada guion tiene 3 escenas (una por foto que se va a generar
                a partir de la original) y sirve tambien para un video corto vertical de unos 9 segundos.
                """.formatted(businessName, blankTo(description, "(sin descripcion)"), network.getLabel());

        StructuredMessageCreateParams<ScriptDrafts> params = StructuredMessageCreateParams.<ScriptDrafts>builder()
                .model(model)
                .maxTokens(16000L)
                .system(SYSTEM)
                .outputConfig(StructuredOutputConfig.<ScriptDrafts>builder()
                        .format(ScriptDrafts.class)
                        .effort(OutputConfig.Effort.MEDIUM)
                        .build())
                // si el modelo rechaza la solicitud, la API reintenta con el modelo de respaldo que recomienda
                .putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                .addUserMessageOfContentBlockParams(List.of(
                        ContentBlockParam.ofImage(Base64ImageSource.builder()
                                .mediaType(Base64ImageSource.MediaType.IMAGE_JPEG)
                                .data(Base64.getEncoder().encodeToString(jpeg))
                                .build()),
                        ContentBlockParam.ofText(prompt)))
                .build();

        StructuredMessage<ScriptDrafts> response = client.messages().create(params);

        StopReason stop = response.stopReason().orElse(null);
        if (StopReason.REFUSAL.equals(stop) || StopReason.MAX_TOKENS.equals(stop)) {
            throw new IllegalStateException("Claude no devolvio guiones (" + stop + ")");
        }

        List<Script> scripts = response.content().stream()
                .flatMap(block -> block.text().stream())
                .flatMap(text -> text.text().scripts().stream())
                .limit(3)
                .map(Drafts::toScript)
                .toList();

        if (scripts.size() < 3) {
            throw new IllegalStateException("Claude devolvio " + scripts.size() + " guiones");
        }
        return scripts;
    }

    @Override
    public String name() {
        return "claude (" + model + ")";
    }

    private static String blankTo(String s, String fallback) {
        return s == null || s.isBlank() ? fallback : s;
    }
}
