package net.maxurhino.doodley_editor.util.render.texture;

import net.maxurhino.doodley_editor.util.Paths;
import net.maxurhino.doodley_editor.util.commons.ImageLoaders;
import net.maxurhino.doodley_editor.util.interfaces.Destroyable;
import net.maxurhino.doodley_editor.util.render.enums.Filtering;
import net.maxurhino.doodley_editor.util.render.texture.loaders.Loader;
import org.joml.*;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Texture implements Destroyable {
    private final int id;
    private final int width;
    private final int height;

    public Texture(Path path) {
        this(path, Filtering.LINEAR);
    }

    public Texture(String path) {
        this(path, Filtering.LINEAR);
    }

    public Texture(Path path, Filtering filtering) {
        this(loadImage(path), filtering);
    }

    private static BufferedImage loadImage(Path path) {
        String fileExtension = Paths.getFileExtension(path);
        if (!ImageLoaders.loaders.containsKey(fileExtension)) {
            throw new IllegalArgumentException(path + " is not a valid image file extension");
        }
        Loader loader = ImageLoaders.loaders.get(fileExtension).get();
        return loader.load(path);
    }

    public Texture(String path, Filtering filtering) {
        this(java.nio.file.Paths.get(path), filtering);
    }

    public Texture(BufferedImage image, Filtering filtering) {
        this.width = image.getWidth();
        this.height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, this.width, this.height, null, 0, this.width);

        IntBuffer buffer;

        buffer = memAllocInt(pixels.length);
        buffer.put(pixels).flip();

        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtering.getId());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtering.getId());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(
                GL_TEXTURE_2D, 0, GL_RGBA8,
                this.width, this.height, 0,
                GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV,
                buffer
        );

        memFree(buffer);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
    }

    public void renderWithTexture(Runnable objects) {
        this.bind();
        glEnable(GL_TEXTURE_2D);
        objects.run();
        glDisable(GL_TEXTURE_2D);
        this.unbind();
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public Vector2i getSize() {
        return new Vector2i(width, height);
    }

    @Override
    public void destroy() {
        glDeleteTextures(this.id);
    }
}
