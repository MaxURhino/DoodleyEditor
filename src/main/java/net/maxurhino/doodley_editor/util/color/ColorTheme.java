package net.maxurhino.doodley_editor.util.color;

import java.awt.*;

public record ColorTheme(
        String name,
        Color control,
        Color checkerboardLight,
        Color checkerboardDark,
        Color popup,
        Color popupShadow
) {
}
