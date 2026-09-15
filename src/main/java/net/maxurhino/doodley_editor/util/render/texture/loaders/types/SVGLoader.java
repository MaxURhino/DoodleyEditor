package net.maxurhino.doodley_editor.util.render.texture.loaders.types;

import net.maxurhino.doodley_editor.util.render.enums.Filtering;
import net.maxurhino.doodley_editor.util.render.texture.Texture;
import net.maxurhino.doodley_editor.util.render.texture.loaders.Loader;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SVGLoader implements Loader {
    public static BufferedImage loadSVG(Path file, Color color) throws Exception {
        return loadSVG(Files.newInputStream(file), color);
    }

    public static BufferedImage loadSVG(InputStream input, Color color) throws Exception {
        String svgText = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        svgText = svgText.replace("currentColor", toHex(color));

        TranscoderInput svgInput = new TranscoderInput(new StringReader(svgText));

        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();

        transcoder.addTranscodingHint(
                PNGTranscoder.KEY_WIDTH,
                64f
        );

        transcoder.addTranscodingHint(
                PNGTranscoder.KEY_HEIGHT,
                64f
        );

        transcoder.transcode(svgInput, null);

        return transcoder.getBufferedImage();
    }

    private static String toHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Texture loadSVGAsImage(Path file, Color color) throws Exception {
        return new Texture(loadSVG(file, color), Filtering.BLINEAR);
    }

    public static Texture loadSVGAsImage(InputStream input, Color color) throws Exception {
        return new Texture(loadSVG(input, color), Filtering.BLINEAR);
    }

    private static class BufferedImageTranscoder extends ImageTranscoder {

        private BufferedImage image;

        @Override
        public BufferedImage createImage(int width, int height) {
            return new BufferedImage(
                    width,
                    height,
                    BufferedImage.TYPE_INT_ARGB
            );
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            image = img;
        }

        public BufferedImage getBufferedImage() {
            return image;
        }
    }

    @Override
    public BufferedImage load(Path path) {
        try {
            return loadSVG(path, Color.WHITE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
