package net.maxurhino.doodley_editor.util.render;

import net.maxurhino.doodley_editor.util.ColorUtil;
import net.maxurhino.doodley_editor.util.interfaces.Destroyable;
import net.maxurhino.doodley_editor.util.render.font.Font;
import net.maxurhino.doodley_editor.util.render.font.TextPlacement;
import net.maxurhino.doodley_editor.util.render.font.TextRenderer;
import net.maxurhino.doodley_editor.util.render.shader.WaveShader;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.lang.Math;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Renderer implements Destroyable {
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

    @Override
    public void destroy() {
        this.draw.destroy();
    }

    public static class Draw implements Destroyable {
        private final Renderer renderer;
        private final WaveShader waveShader;

        public Draw(net.maxurhino.doodley_editor.Window window, Renderer renderer) {
            this.renderer = renderer;

            this.waveShader = new WaveShader();
        }

        public void wavyText(float time, float amplitude, float frequency, float speed, Font font, String text, Vector2f pos, Color color) {
            waveShader.setTime(time);
            waveShader.setAmplitude(amplitude);
            waveShader.setFrequency(frequency);
            waveShader.setSpeed(speed);

            waveShader.render(() -> text(font, text, pos, color));
        }

        public void text(Font font, String text, Vector2f pos, Color color) {
            renderer.onRenderObject();
            TextRenderer.drawText(font, text, pos.x, pos.y, color);
        }

        public void text(Font font, String text, TextPlacement placement, TextPlacement.Placement hAlign, TextPlacement.Placement vAlign, Color color) {
            renderer.onRenderObject();
            TextRenderer.drawText(font, text, placement, hAlign, vAlign, color);
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

        public void triangle(Vector2f pos1, Vector2f pos2, Vector2f pos3, Color color) {
            renderer.onRenderObject();

            glBegin(GL_TRIANGLES);

            setVertexColor(color);

            glVertex2f(pos1.x, pos1.y);
            glVertex2f(pos2.x, pos2.y);
            glVertex2f(pos3.x, pos3.y);

            glEnd();
        }

        private void fillCircleShape(Vector2f center, float radius, int segments, float startAngleDeg, float endAngleDeg, Color color) {
            glBegin(GL_TRIANGLE_FAN);

            setVertexColor(color);

            glVertex2f(center.x, center.y);

            double startRad = Math.toRadians(startAngleDeg);
            double endRad = Math.toRadians(endAngleDeg);

            for (int i = 0; i <= segments; i++) {
                double t = (double) i / segments;
                double angle = startRad + t * (endRad - startRad);
                float x = center.x + (float) Math.cos(angle) * radius;
                float y = center.y + (float) Math.sin(angle) * radius;
                glVertex2f(x, y);
            }

            glEnd();
        }

        public void circle(Vector2f center, float radius, int segments, Color color) {
            renderer.onRenderObject();
            fillCircleShape(center, radius, segments, 0, 360, color);
        }

        public void circle(Vector2f center, float radius, Color color) {
            this.circle(center, radius, 20, color);
        }

        public void roundedRect(Vector2f pos, Vector2f size, float radius, Color color) {
            renderer.onRenderObject();

            fillCircleShape(new Vector2f(pos).add(radius, radius), radius, 20, 180, 270, color); // top-left
            fillCircleShape(new Vector2f(pos).add(size.x - radius, radius), radius, 20, 270, 360, color); // top-right
            fillCircleShape(new Vector2f(pos).add(size.x - radius, size.y - radius), radius, 20, 0, 90, color); // bottom-right
            fillCircleShape(new Vector2f(pos).add(radius, size.y - radius), radius, 20, 90, 180, color); // bottom-left

            if (size.x > radius * 2) {
                rect(
                        new Vector2f(pos).add(radius, 0),
                        new Vector2f(size.x - radius * 2, size.y),
                        color
                );
            }
            if (size.y > radius * 2) {
                rect(
                        new Vector2f(pos).add(0, radius),
                        new Vector2f(radius, size.y - radius * 2),
                        color
                );
                rect(
                        new Vector2f(pos).add(size.x - radius, radius),
                        new Vector2f(radius, size.y - radius * 2),
                        color
                );
            }
        }

        @Override
        public void destroy() {
            waveShader.destroy();
        }
    }
}
