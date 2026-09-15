package net.maxurhino.doodley_editor.util.render.texture.loaders;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface Loader {
    BufferedImage load(Path path);
}
