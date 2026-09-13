package net.maxurhino.doodley_editor.util;

import org.joml.*;

import java.awt.*;

public class ColorUtil {
    public static Vector4f getFromColor(Color color) {
        return new Vector4f(
                color.getRed  () / 255f,
                color.getGreen() / 255f,
                color.getBlue () / 255f,
                color.getAlpha() / 255f
        );
    }
}
