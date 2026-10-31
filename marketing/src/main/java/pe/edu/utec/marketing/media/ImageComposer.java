package pe.edu.utec.marketing.media;

import org.springframework.stereotype.Component;
import pe.edu.utec.marketing.model.Look;
import pe.edu.utec.marketing.model.Scene;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Arma cada foto de la campaña a partir de la original:
 * recorte distinto por escena, filtro de color, degradado y texto encima.
 */
@Component
public class ImageComposer {

    // zoom y punto focal (x, y en 0..1) para que las 3 fotos no salgan iguales
    private static final double[][] FRAMING = {
            {1.00, 0.50, 0.50},
            {1.25, 0.35, 0.40},
            {1.45, 0.65, 0.60}
    };

    public BufferedImage compose(BufferedImage src, Scene scene, int index, String brand, int width, int height) {
        return compose(src, scene, index, brand, width, height, false);
    }

    /**
     * @param forVideo deja margenes mas grandes porque el zoom del video recorta los bordes
     *                 y en reels/tiktok la parte de abajo la tapa la interfaz
     */
    public BufferedImage compose(BufferedImage src, Scene scene, int index, String brand, int width, int height,
                                 boolean forVideo) {
        int side = forVideo ? width / 9 : width / 14;
        int top = forVideo ? height / 11 : width / 14;
        int bottom = forVideo ? height / 6 : width / 14;

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        double[] f = FRAMING[index % FRAMING.length];
        drawCover(g, src, width, height, f[0], f[1], f[2]);
        g.dispose();

        applyLook(out, scene.getLook());

        g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawGradient(g, width, height);
        drawOverlay(g, scene.getOverlay(), width, height, side, bottom);
        drawBrand(g, brand, width, side, top);
        g.dispose();
        return out;
    }

    private void drawCover(Graphics2D g, BufferedImage src, int w, int h, double zoom, double fx, double fy) {
        double scale = Math.max((double) w / src.getWidth(), (double) h / src.getHeight()) * zoom;
        int sw = (int) Math.ceil(src.getWidth() * scale);
        int sh = (int) Math.ceil(src.getHeight() * scale);
        int x = (int) Math.round(-(sw - w) * fx);
        int y = (int) Math.round(-(sh - h) * fy);
        g.drawImage(src, x, y, sw, sh, null);
    }

    void applyLook(BufferedImage img, Look look) {
        if (look == null) return;
        int w = img.getWidth();
        int h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        float[] hsb = new float[3];
        for (int i = 0; i < px.length; i++) {
            int r = (px[i] >> 16) & 0xff;
            int gr = (px[i] >> 8) & 0xff;
            int b = px[i] & 0xff;
            switch (look) {
                case WARM -> {
                    r = clamp(r * 1.08 + 6);
                    gr = clamp(gr * 1.02);
                    b = clamp(b * 0.88);
                }
                case COOL -> {
                    r = clamp(r * 0.92);
                    gr = clamp(gr * 1.0);
                    b = clamp(b * 1.10 + 6);
                }
                case MONO -> {
                    int y = clamp(0.299 * r + 0.587 * gr + 0.114 * b);
                    // un poco mas de contraste para que no se vea lavado
                    y = clamp((y - 128) * 1.12 + 128);
                    r = y;
                    gr = y;
                    b = y;
                }
                case VIVID -> {
                    Color.RGBtoHSB(r, gr, b, hsb);
                    int rgb = Color.HSBtoRGB(hsb[0], Math.min(1f, hsb[1] * 1.3f), Math.min(1f, hsb[2] * 1.04f));
                    r = (rgb >> 16) & 0xff;
                    gr = (rgb >> 8) & 0xff;
                    b = rgb & 0xff;
                }
            }
            px[i] = (r << 16) | (gr << 8) | b;
        }
        img.setRGB(0, 0, w, h, px, 0, w);
    }

    private void drawGradient(Graphics2D g, int w, int h) {
        int top = (int) (h * 0.55);
        g.setPaint(new GradientPaint(0, top, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 190)));
        g.fillRect(0, top, w, h - top);
    }

    private void drawOverlay(Graphics2D g, String text, int w, int h, int margin, int bottom) {
        if (text == null || text.isBlank()) return;
        int size = w / 12;
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, size);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        // si no entra en 3 lineas se baja el tamaño
        List<String> lines = wrap(text.toUpperCase(), fm, w - margin * 2);
        while (lines.size() > 3 && size > w / 22) {
            size -= 4;
            font = font.deriveFont((float) size);
            g.setFont(font);
            fm = g.getFontMetrics();
            lines = wrap(text.toUpperCase(), fm, w - margin * 2);
        }

        int lineHeight = (int) (fm.getHeight() * 0.95);
        int y = h - bottom - lineHeight * (lines.size() - 1);
        for (String line : lines) {
            g.setColor(new Color(0, 0, 0, 120));
            g.drawString(line, margin + 3, y + 3);
            g.setColor(Color.WHITE);
            g.drawString(line, margin, y);
            y += lineHeight;
        }

        g.setColor(new Color(255, 196, 0));
        g.fillRect(margin, h - bottom - lineHeight * lines.size() - size / 4, w / 8, Math.max(6, size / 9));
    }

    private void drawBrand(Graphics2D g, String brand, int w, int x, int y) {
        if (brand == null || brand.isBlank()) return;
        int pad = w / 40;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, w / 30));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(brand);
        int bh = fm.getHeight() + pad;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, tw + pad * 2, bh, bh, bh);
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(20, 20, 20));
        g.drawString(brand, x + pad, y + pad / 2 + fm.getAscent());
    }

    static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth || line.isEmpty()) {
                line.setLength(0);
                line.append(candidate);
            } else {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    private static int clamp(double v) {
        return (int) Math.max(0, Math.min(255, Math.round(v)));
    }
}
