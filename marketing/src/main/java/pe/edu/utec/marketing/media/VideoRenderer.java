package pe.edu.utec.marketing.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Video vertical tipo reel con ffmpeg: cada foto dura unos segundos con un zoom lento
 * y transicion de fundido entre ellas.
 */
@Component
public class VideoRenderer {

    private static final Logger log = LoggerFactory.getLogger(VideoRenderer.class);

    static final int FPS = 30;
    static final double SECONDS_PER_FRAME = 3.0;
    static final double FADE = 0.5;

    private final String ffmpeg;

    public VideoRenderer(@Value("${app.ffmpeg:ffmpeg}") String ffmpeg) {
        this.ffmpeg = ffmpeg;
    }

    public void render(List<Path> frames, Path output, int width, int height) {
        List<String> cmd = buildCommand(frames, output, width, height);
        try {
            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(2, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new IllegalStateException("ffmpeg tardo demasiado");
            }
            if (process.exitValue() != 0) {
                log.error("ffmpeg fallo:\n{}", tail(out));
                throw new IllegalStateException("ffmpeg termino con codigo " + process.exitValue());
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo ejecutar ffmpeg, ¿esta instalado?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Generacion de video interrumpida", e);
        }
    }

    List<String> buildCommand(List<Path> frames, Path output, int width, int height) {
        List<String> cmd = new ArrayList<>(List.of(ffmpeg, "-y", "-loglevel", "error"));
        // cada foto entra como un solo cuadro; zoompan es el que genera los n cuadros de su duracion
        for (Path frame : frames) {
            cmd.addAll(List.of("-i", frame.toString()));
        }

        int n = (int) (SECONDS_PER_FRAME * FPS);
        StringBuilder filter = new StringBuilder();
        for (int i = 0; i < frames.size(); i++) {
            // se escala al doble antes del zoompan para que el zoom no se vea pixelado
            filter.append("[%d:v]scale=%d:%d,zoompan=z='min(zoom+0.0015,1.12)':d=%d:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s=%dx%d:fps=%d,setsar=1[v%d];"
                    .formatted(i, width * 2, height * 2, n, width, height, FPS, i));
        }
        String last = "v0";
        double offset = SECONDS_PER_FRAME - FADE;
        for (int i = 1; i < frames.size(); i++) {
            String label = "x" + i;
            filter.append("[%s][v%d]xfade=transition=fade:duration=%s:offset=%s[%s];"
                    .formatted(last, i, FADE, offset, label));
            last = label;
            offset += SECONDS_PER_FRAME - FADE;
        }
        filter.append("[%s]format=yuv420p[out]".formatted(last));

        cmd.addAll(List.of(
                "-filter_complex", filter.toString(),
                "-map", "[out]",
                "-c:v", "libx264", "-preset", "veryfast", "-crf", "23",
                "-r", String.valueOf(FPS),
                "-movflags", "+faststart",
                output.toString()));
        return cmd;
    }

    private static String tail(String s) {
        return s.length() > 2000 ? s.substring(s.length() - 2000) : s;
    }
}
