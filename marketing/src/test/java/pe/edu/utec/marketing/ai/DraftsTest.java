package pe.edu.utec.marketing.ai;

import org.junit.jupiter.api.Test;
import pe.edu.utec.marketing.model.Look;
import pe.edu.utec.marketing.model.Network;
import pe.edu.utec.marketing.model.Script;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DraftsTest {

    @Test
    void completaEscenasFaltantesYLimpiaHashtags() {
        ScriptDrafts.Draft draft = new ScriptDrafts.Draft("Titulo", "Precio", "Gancho",
                List.of(new ScriptDrafts.DraftScene("Solo una", null)),
                "Texto", List.of("pan", "#Pan Rico", "pan"), "Escribenos");

        Script s = Drafts.toScript(draft);

        assertEquals(3, s.getScenes().size());
        assertEquals(Look.VIVID, s.getScenes().get(0).getLook());
        assertEquals("#pan #PanRico", s.getHashtags());
    }

    @Test
    void recortaEscenasExtra() {
        var scene = new ScriptDrafts.DraftScene("x", Look.MONO);
        ScriptDrafts.Draft draft = new ScriptDrafts.Draft("t", "a", "h",
                List.of(scene, scene, scene, scene, scene), "c", List.of(), null);

        assertEquals(3, Drafts.toScript(draft).getScenes().size());
    }

    @Test
    void cortaTextosLargos() {
        String largo = "a".repeat(500);
        assertEquals(120, Drafts.cut(largo, 120).length());
        assertEquals("", Drafts.cut(null, 10));
    }

    @Test
    void plantillasDevuelvenTresGuionesDistintos() {
        List<Script> scripts = new TemplateScriptGenerator()
                .generate(new byte[0], "Café Ñaña", "cafe de especialidad", Network.TIKTOK);

        assertEquals(3, scripts.size());
        assertEquals(3, scripts.stream().map(Script::getAngle).distinct().count());
        assertTrue(scripts.get(0).getHashtags().contains("#cafenana"));
        assertTrue(scripts.get(0).fullCaption().contains("#fyp #parati"));
    }
}
