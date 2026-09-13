package net.maxurhino.doodley_editor.util.render;

import net.maxurhino.doodley_editor.util.ColorUtil;
import net.maxurhino.doodley_editor.util.render.font.GLFont;
import net.maxurhino.doodley_editor.util.render.font.TextRenderer;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.lang.Math;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private Color clearColor;
    private final net.maxurhino.doodley_editor.Window window;
    public final Draw draw;

    public Renderer(net.maxurhino.doodley_editor.Window window) {
        this.pose = new Matrix3x2fStack(16);
        this.window = window;
        this.draw = new Draw(window, this);
    }

    public void setClearColor(Color color) {
        this.clearColor = color;

        Vector4f colorV = ColorUtil.getFromColor(color);
        glClearColor(colorV.x, colorV.y, colorV.z, colorV.w);
    }

    public Color getClearColor() {
        return clearColor;
    }

    private final FloatBuffer projBuf = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer modelBuf = BufferUtils.createFloatBuffer(16);

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        projBuf.clear();
        new Matrix4f().ortho2D(0, window.currentSize.x, window.currentSize.y, 0).get(projBuf);
        glLoadMatrixf(projBuf);
    }

    private final Matrix3x2fStack pose;

    public Matrix3x2fStack pose() {
        return this.pose;
    }

    public static void setVertexColor(Color color) {
        Vector4f colorV = ColorUtil.getFromColor(color);
        glColor4f(colorV.x, colorV.y, colorV.z, colorV.w);
    }

    public void scissor(Vector2i pos, Vector2i size, Runnable run) {
        glEnable(GL_SCISSOR_TEST);
        glScissor(pos.x, window.currentSize.y - pos.y - size.y, size.x, size.y);
        run.run();
        glDisable(GL_SCISSOR_TEST);
    }

    public void beginMask(Runnable maskShape) {
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);

        glColorMask(false, false, false, false);
        glDepthMask(false);
        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

        maskShape.run();

        glColorMask(true, true, true, true);
        glDepthMask(true);

        glStencilFunc(GL_EQUAL, 1, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }

    private void onRenderObject() {
        glMatrixMode(GL_MODELVIEW);
        modelBuf.clear();
        pose.get4x4(modelBuf);
        glLoadMatrixf(modelBuf);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void endMask() {
        glDisable(GL_STENCIL_TEST);
    }

    public static class Draw {
        private final net.maxurhino.doodley_editor.Window window;
        private final Renderer renderer;

        public Draw(net.maxurhino.doodley_editor.Window window, Renderer renderer) {
            this.window = window;
            this.renderer = renderer;
        }

        public void text(GLFont font, String text, Vector2f pos, Color color) {
            renderer.onRenderObject();
            TextRenderer.drawText(font, text, pos.x, pos.y, color);
        }

        public void rect(Vector2f pos, Vector2f size, Color color) {
            renderer.onRenderObject();

            glBegin(GL_QUADS);

            setVertexColor(color);

            glTexCoord2i(0, 0);
            glVertex2f(pos.x + 0,      pos.y + 0     );
            glTexCoord2i(1, 0);
            glVertex2f(pos.x + size.x, pos.y + 0     );
            glTexCoord2i(1, 1);
            glVertex2f(pos.x + size.x, pos.y + size.y);
            glTexCoord2i(0, 1);
            glVertex2f(pos.x + 0,      pos.y + size.y);

            glEnd();
        }

        private void fillCircleShape(Vector2f center, float radius, int segments) {
            glBegin(GL_TRIANGLE_FAN);

            setVertexColor(Color.WHITE);

            glVertex2f(center.x, center.y);

            for (int i = 0; i <= segments; i++) {
                double angle = 2.0 * Math.PI * i / segments;
                float x = center.x + (float) Math.cos(angle) * radius;
                float y = center.y + (float) Math.sin(angle) * radius;
                glVertex2f(x, y);
            }

            glEnd();
        }

        public void circle(Vector2f center, float radius, int segments, Color color) {
            renderer.onRenderObject();

            renderer.beginMask(() -> fillCircleShape(center, radius, segments));
            this.rect(
                    new Vector2f(center).sub(radius, radius),
                    new Vector2f(radius * 2, radius * 2),
                    color
            );
            renderer.endMask();
        }

        public void circle(Vector2f center, float radius, Color color) {
            this.circle(center, radius, 20, color);
        }

        public void triangle(Vector2f pos1, Vector2f pos2, Vector2f pos3, Color color) {
            renderer.onRenderObject();

            glBegin(GL_TRIANGLES);

            setVertexColor(color);

            glVertex2f(pos1.x, pos1.y);
            glVertex2f(pos2.x, pos2.y);
            glVertex2f(pos3.x, pos3.y);

            glEnd();
        }

        public void roundedRect(Vector2f pos, Vector2f size, float radius, Color color) {
            renderer.onRenderObject();

            this.renderer.beginMask(() -> {
                fillCircleShape(new Vector2f(pos).add(radius, radius), radius, 20);
                fillCircleShape(new Vector2f(pos).add(size.x - radius, radius), radius, 20);
                fillCircleShape(new Vector2f(pos).add(radius, size.y - radius), radius, 20);
                fillCircleShape(new Vector2f(pos).add(size.x - radius, size.y - radius), radius, 20);

                // The straight rect() calls here are fine to keep as-is — they only
                // ever draw a plain filled quad, they don't manage stencil state at all.
                rect(
                        new Vector2f(pos).add(radius, 0),
                        new Vector2f(size).sub(radius * 2, 0),
                        Color.WHITE
                );
                rect(
                        new Vector2f(pos).add(0, radius),
                        new Vector2f(size).sub(0, radius * 2),
                        Color.WHITE
                );
            });

            this.rect(pos, size, color);
            this.renderer.endMask();
        }
    }
}
