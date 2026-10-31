package pe.edu.utec.marketing.ai;

import pe.edu.utec.marketing.model.Network;
import pe.edu.utec.marketing.model.Script;

import java.util.List;

public interface ScriptGenerator {

    /**
     * @param jpeg foto del producto ya redimensionada
     * @return siempre 3 guiones
     */
    List<Script> generate(byte[] jpeg, String businessName, String description, Network network);

    String name();
}
