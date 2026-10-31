package pe.edu.utec.marketing.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

// guarda archivos en disco (en docker es un volumen montado en /data/media)
@Component
public class MediaStorage {

    private final Path root;

    public MediaStorage(@Value("${app.media-dir}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear " + root, e);
        }
    }

    public String newFolder() {
        String folder = UUID.randomUUID().toString();
        try {
            Files.createDirectories(root.resolve(folder));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return folder;
    }

    public String save(String folder, String fileName, byte[] data) {
        String relative = folder + "/" + fileName;
        try {
            Files.write(resolve(relative), data);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar " + relative, e);
        }
        return relative;
    }

    public byte[] read(String relative) {
        try {
            return Files.readAllBytes(resolve(relative));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + relative, e);
        }
    }

    public Path resolve(String relative) {
        Path p = root.resolve(relative).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("Ruta invalida: " + relative);
        }
        return p;
    }

    public Path getRoot() {
        return root;
    }
}
