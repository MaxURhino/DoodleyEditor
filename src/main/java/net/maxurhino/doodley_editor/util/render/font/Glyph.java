package net.maxurhino.doodley_editor.util.render.font;

import org.joml.Vector2f;

import java.util.List;

public record Glyph(List<List<Vector2f>> contours, float advance, float bearingX, float bearingY, float width,
                    float height) {
}
