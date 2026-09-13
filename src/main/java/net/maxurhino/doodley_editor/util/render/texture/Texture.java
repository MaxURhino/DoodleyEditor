package net.maxurhino.doodley_editor.util.render.texture;

import net.maxurhino.doodley_editor.util.interfaces.Destroyable;
import net.maxurhino.doodley_editor.util.render.enums.Filtering;
import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.*;
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
        this(path.toAbsolutePath().toString(), filtering);
    }

    public Texture(String path, Filtering filtering) {
        ByteBuffer image;
        int w, h;

        try (MemoryStack stack = stackPush()) {
            IntBuffer wBuf = stack.mallocInt(1);
            IntBuffer hBuf = stack.mallocInt(1);
            IntBuffer channelsBuf = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);

            image = stbi_load(path, wBuf, hBuf, channelsBuf, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture \"" + path + "\": " + stbi_failure_reason());
            }

            w = wBuf.get(0);
            h = hBuf.get(0);
        }

        this.width = w;
        this.height = h;

        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtering.getId());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtering.getId());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

        stbi_image_free(image);
    }

    public Texture(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, this.width, this.height, null, 0, this.width);

        boolean shouldFlip = false;

        IntBuffer buffer;

        if (shouldFlip) {
            int[] flipped = new int[pixels.length];
            for (int y = 0; y < this.height; y++) {
                System.arraycopy(
                        pixels, y * this.width,
                        flipped, (this.height - 1 - y) * this.width,
                        this.width
                );
            }

            buffer = memAllocInt(flipped.length);
            buffer.put(flipped).flip();
        } else {
            buffer = memAllocInt(pixels.length);
            buffer.put(pixels).flip();
        }

        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
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
