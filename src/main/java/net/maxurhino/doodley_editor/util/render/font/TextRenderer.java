package net.maxurhino.doodley_editor.util.render.font;

import net.maxurhino.doodley_editor.util.interfaces.Renderable;
import net.maxurhino.doodley_editor.util.render.Renderer;
import org.joml.Vector2f;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class TextRenderer {
    public static void drawText(Font font, String text, float x, float y, Color color) {
        new TextDrawer(font, text, x, y, color).render();
    }

    public static void drawText(Font font, String text, TextPlacement textPlacement, TextPlacement.Placement hAlign, TextPlacement.Placement vAlign, Color color) {
        drawText(
                font,
                text,
                textPlacement.getX(hAlign),
                textPlacement.getY(vAlign),
                color
        );
    }

    public static float getTextWidth(Font font, String text) {
        if (font == null) return 0;

        float scale = font.getScale();
        float width = 0;

        for (char c : text.toCharArray()) {
            Glyph glyph = font.getGlyph(c);
            if (glyph == null) continue;
            width += glyph.advance() * scale;
        }

        return width;
    }

    public record TextDrawer(
            Font font,
            String text,
            float x,
            float y,
            Color color
    ) implements Renderable {

        @Override
        public void render() {
            if (this.font == null) return;

            float scale = font.getScale();
            float currentX = x;
            float baselineY = y + font.getLineHeight();

            for (char c : text.toCharArray()) {
                Glyph glyph = font.getGlyph(c);
                if (glyph == null) continue;

                drawGlyph(glyph, currentX, baselineY, scale, color);

                currentX += glyph.advance() * scale;
            }
        }

        private static void drawGlyph(Glyph glyph, float originX, float baselineY, float scale, Color color) {
            if (glyph.contours().isEmpty()) return; // e.g. space — advance still applies in render()

            // Stencil pass: triangle-fan each contour with GL_INVERT.
            // Overlapping fans (e.g. an outer ring + an inner hole in "O") cancel out correctly —
            // no polygon triangulation library needed, same trick as Renderer's circle()/roundedRect() masks.
            glEnable(GL_STENCIL_TEST);
            glClear(GL_STENCIL_BUFFER_BIT);
            glColorMask(false, false, false, false);
            glDepthMask(false);
            glStencilFunc(GL_ALWAYS, 0, 0xFF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_INVERT);

            for (List<Vector2f> contour : glyph.contours()) {
                glBegin(GL_TRIANGLE_FAN);
                glVertex2f(originX, baselineY); // fan pivot — any fixed point works with the invert trick
                for (Vector2f p : contour) {
                    glVertex2f(originX + p.x * scale, baselineY - p.y * scale); // flip y: FreeType is y-up, screen is y-down
                }
                Vector2f first = contour.getFirst();
                glVertex2f(originX + first.x * scale, baselineY - first.y * scale); // close the fan
                glEnd();
            }

            // Cover pass: fill the glyph's bounding box wherever the stencil ended up non-zero
            glColorMask(true, true, true, true);
            glDepthMask(true);
            glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
            glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

            Renderer.setVertexColor(color);

            float left   = originX + glyph.bearingX() * scale;
            float right  = left + glyph.width() * scale;
            float top    = baselineY - glyph.bearingY() * scale;
            float bottom = top + glyph.height() * scale;

            glBegin(GL_QUADS);
            glVertex2f(left,  top);
            glVertex2f(right, top);
            glVertex2f(right, bottom);
            glVertex2f(left,  bottom);
            glEnd();

            glDisable(GL_STENCIL_TEST);
        }
    }
}