package pe.edu.utec.marketing.media;

import org.junit.jupiter.api.Test;
import pe.edu.utec.marketing.model.Look;
import pe.edu.utec.marketing.model.Scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaTest {

    private BufferedImage sample(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        g.setColor(new Color(200, 120, 60));
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }

    @Test
    void componeConElTamanoPedido() {
        ImageComposer composer = new ImageComposer();
        BufferedImage out = composer.compose(sample(800, 600), new Scene("Hola mundo", Look.WARM), 1, "Marca", 1080, 1350);

        assertEquals(1080, out.getWidth());
        assertEquals(1350, out.getHeight());
    }

    @Test
    void filtroMonoDejaGrises() {
        ImageComposer composer = new ImageComposer();
        BufferedImage img = sample(10, 10);
        composer.applyLook(img, Look.MONO);

        int rgb = img.getRGB(5, 5);
        int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
        assertEquals(r, g);
        assertEquals(g, b);
    }

    @Test
    void fitNoAgrandaImagenesChicas() {
        assertEquals(300, Images.fit(sample(300, 200), 1568).getWidth());
        assertEquals(1568, Images.fit(sample(4000, 3000), 1568).getWidth());
    }

    @Test
    void comandoFfmpegUsaUnaEntradaPorFoto() {
        VideoRenderer renderer = new VideoRenderer("ffmpeg");
        List<String> cmd = renderer.buildCommand(
                List.of(Path.of("a.jpg"), Path.of("b.jpg"), Path.of("c.jpg")), Path.of("out.mp4"), 720, 1280);

        assertEquals(3, cmd.stream().filter("-i"::equals).count());
        assertTrue(cmd.stream().noneMatch("-loop"::equals));
        String filter = cmd.get(cmd.indexOf("-filter_complex") + 1);
        assertTrue(filter.contains("xfade"));
        assertEquals("out.mp4", cmd.getLast());
    }
}
