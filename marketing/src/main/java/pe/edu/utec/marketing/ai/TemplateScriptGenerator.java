package pe.edu.utec.marketing.ai;

import pe.edu.utec.marketing.model.Look;
import pe.edu.utec.marketing.model.Network;
import pe.edu.utec.marketing.model.Script;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

// guiones con plantillas, para cuando no hay API key o la API falla
public class TemplateScriptGenerator implements ScriptGenerator {

    @Override
    public List<Script> generate(byte[] jpeg, String businessName, String description, Network network) {
        String name = businessName.strip();
        String about = description == null || description.isBlank()
                ? "lo que hacemos con dedicacion todos los dias"
                : description.strip();
        String tag = "#" + slug(name);
        String red = network == Network.TIKTOK ? "#fyp #parati" : "#instagood #emprendimiento";

        ScriptDrafts.Draft beneficio = new ScriptDrafts.Draft(
                "Lo que nos hace distintos",
                "Beneficio",
                "¿Buscabas algo asi? Te presentamos " + name,
                List.of(scene("Conoce " + name, Look.VIVID),
                        scene("Hecho con calidad", Look.WARM),
                        scene("Te va a encantar", Look.VIVID)),
                "En " + name + " nos enfocamos en " + about + ". Cada detalle esta pensado para ti.",
                List.of(tag, "#hechoenperu", "#calidad", red),
                "Escribenos por DM y te contamos mas");

        ScriptDrafts.Draft emocional = new ScriptDrafts.Draft(
                "Detras de cada detalle",
                "Emocional",
                "Esto empezo como un sueño",
                List.of(scene("Empezo como un sueño", Look.MONO),
                        scene("Hoy lo compartimos contigo", Look.WARM),
                        scene(name, Look.WARM)),
                "Detras de " + name + " hay personas que ponen el corazon en " + about
                        + ". Gracias por acompañarnos en este camino.",
                List.of(tag, "#emprendedores", "#apoyaloslocal", red),
                "Siguenos para ver mas");

        ScriptDrafts.Draft directo = new ScriptDrafts.Draft(
                "Directo al punto",
                "Llamado a la accion",
                "3 segundos para convencerte",
                List.of(scene("Mira esto", Look.COOL),
                        scene("Asi de simple", Look.VIVID),
                        scene("Pidelo hoy", Look.COOL)),
                name + ": " + about + ". Sin complicaciones, directo a lo que necesitas.",
                List.of(tag, "#novedad", "#lima", red),
                "Haz tu pedido por mensaje directo");

        return List.of(beneficio, emocional, directo).stream().map(Drafts::toScript).toList();
    }

    @Override
    public String name() {
        return "plantillas";
    }

    private static ScriptDrafts.DraftScene scene(String text, Look look) {
        return new ScriptDrafts.DraftScene(text, look);
    }

    private static String slug(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
