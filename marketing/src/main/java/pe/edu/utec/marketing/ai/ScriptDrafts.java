package pe.edu.utec.marketing.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import pe.edu.utec.marketing.model.Look;

import java.util.List;

// estructura que se le pide al modelo (structured output)
public record ScriptDrafts(List<Draft> scripts) {

    public record Draft(
            @JsonPropertyDescription("Nombre corto del guion, max 6 palabras")
            String title,
            @JsonPropertyDescription("Enfoque del guion: emocional, precio, beneficio, humor, etc.")
            String angle,
            @JsonPropertyDescription("Frase gancho de los primeros 2 segundos del video")
            String hook,
            @JsonPropertyDescription("Exactamente 3 escenas, una por cada foto generada")
            List<DraftScene> scenes,
            @JsonPropertyDescription("Texto del post, 2 a 4 oraciones, sin hashtags")
            String caption,
            @JsonPropertyDescription("Entre 4 y 8 hashtags, cada uno empezando con #")
            List<String> hashtags,
            @JsonPropertyDescription("Llamado a la accion corto")
            String callToAction) {
    }

    public record DraftScene(
            @JsonPropertyDescription("Texto que va encima de la foto, max 8 palabras")
            String overlay,
            @JsonPropertyDescription("Filtro de color para la foto")
            Look look) {
    }
}
