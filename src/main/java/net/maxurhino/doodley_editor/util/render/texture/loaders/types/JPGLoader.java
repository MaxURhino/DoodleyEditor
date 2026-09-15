package net.maxurhino.doodley_editor.util.render.texture.loaders.types;

import net.maxurhino.doodley_editor.util.render.texture.loaders.Loader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class JPGLoader implements Loader {
    @Override
    public BufferedImage load(Path path) {
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                throw new IOException("Failed to load image (unrecognized format or corrupt file): " + path);
            }
            return image;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
