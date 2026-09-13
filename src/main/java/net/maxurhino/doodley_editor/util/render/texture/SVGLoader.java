package net.maxurhino.doodley_editor.util.render.texture;

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

public class SVGLoader {
    public static BufferedImage loadSVG(Path file, int width, int height, Color color) throws Exception {
        return loadSVG(Files.newInputStream(file), width, height, color);
    }

    public static BufferedImage loadSVG(InputStream input, int width, int height, Color color) throws Exception {
        String svgText = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        svgText = svgText.replace("currentColor", toHex(color));

        TranscoderInput svgInput = new TranscoderInput(new StringReader(svgText));

        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(
                PNGTranscoder.KEY_WIDTH,
                (float) width
        );
        transcoder.addTranscodingHint(
                PNGTranscoder.KEY_HEIGHT,
                (float) height
        );

        transcoder.transcode(svgInput, null);

        return transcoder.getBufferedImage();
    }

    private static String toHex(Color color) {
        return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Texture loadSVGAsImage(Path file, int width, int height, Color color) throws Exception {
        return new Texture(loadSVG(file, width, height, color));
    }

    public static Texture loadSVGAsImage(InputStream input, int width, int height, Color color) throws Exception {
        return new Texture(loadSVG(input, width, height, color));
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
}
