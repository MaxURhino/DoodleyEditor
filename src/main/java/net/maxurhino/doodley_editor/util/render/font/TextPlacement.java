package net.maxurhino.doodley_editor.util.render.font;

import org.joml.Vector2f;

public record TextPlacement(Font font, String text, Vector2f pos, Vector2f regionSize) {
    public enum Placement {
        FIRST,
        SECOND,
        THIRD
    }

    public float getX(Placement placement) {
        if (this.font == null) return pos.x;
        float textWidth = TextRenderer.getTextWidth(font, text);
        return switch (placement) {
            case FIRST -> pos.x;
            case SECOND -> ((regionSize.x - textWidth) / 2) + pos.x;
            case THIRD -> (regionSize.x - textWidth) + pos.x;
        };
    }

    public float getY(Placement placement) {
        if (this.font == null) return pos.y;
        float textHeight = font.getLineHeight();
        return switch (placement) {
            case FIRST -> pos.y;
            case SECOND -> ((regionSize.y - textHeight) / 2) + pos.y;
            case THIRD -> (regionSize.y - textHeight) + pos.y;
        };
    }
}
