package net.maxurhino.doodley_editor.util.render.texture.loaders.types;

import net.maxurhino.doodley_editor.util.render.texture.loaders.Loader;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class PNGLoader implements Loader {
    @Override
    public BufferedImage load(Path path) {
        ByteBuffer imageData;
        int width, height;

        try (MemoryStack stack = stackPush()) {
            IntBuffer wBuf = stack.mallocInt(1);
            IntBuffer hBuf = stack.mallocInt(1);
            IntBuffer channelsBuf = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(false); // BufferedImage is already top-left origin — no flip needed here, unlike your GL Texture path

            imageData = stbi_load(path.toAbsolutePath().toString(), wBuf, hBuf, channelsBuf, 4);
            if (imageData == null) {
                throw new RuntimeException("Failed to load image \"" + path + "\": " + stbi_failure_reason());
            }

            width = wBuf.get(0);
            height = hBuf.get(0);
        }

        BufferedImage result = toBufferedImage(imageData, width, height);

        stbi_image_free(imageData);

        return result;
    }

    private static BufferedImage toBufferedImage(ByteBuffer pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        pixels.rewind();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = pixels.get() & 0xFF;
                int g = pixels.get() & 0xFF;
                int b = pixels.get() & 0xFF;
                int a = pixels.get() & 0xFF;

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, argb);
            }
        }

        return image;
    }
}
