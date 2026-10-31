package pe.edu.utec.marketing.ai;

import pe.edu.utec.marketing.model.Look;
import pe.edu.utec.marketing.model.Scene;
import pe.edu.utec.marketing.model.Script;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class Drafts {

    private Drafts() {
    }

    // normaliza lo que venga del modelo para que siempre haya 3 escenas y textos dentro de los limites
    static Script toScript(ScriptDrafts.Draft d) {
        List<Scene> scenes = new ArrayList<>();
        if (d.scenes() != null) {
            for (ScriptDrafts.DraftScene s : d.scenes()) {
                if (scenes.size() == 3) break;
                scenes.add(new Scene(cut(s.overlay(), 140), s.look() == null ? Look.VIVID : s.look()));
            }
        }
        while (scenes.size() < 3) {
            scenes.add(new Scene(cut(d.hook(), 140), Look.values()[scenes.size() % Look.values().length]));
        }

        String tags = d.hashtags() == null ? "" : d.hashtags().stream()
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .map(t -> t.startsWith("#") ? t : "#" + t)
                .map(t -> t.replace(" ", ""))
                .distinct()
                .collect(Collectors.joining(" "));

        return new Script(cut(d.title(), 120), cut(d.angle(), 200), cut(d.hook(), 200), scenes,
                cut(d.caption(), 1800), cut(tags, 500), cut(d.callToAction(), 120));
    }

    static String cut(String s, int max) {
        if (s == null) return "";
        s = s.strip();
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
