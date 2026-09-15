package net.maxurhino.doodley_editor.util.commons;

import net.maxurhino.doodley_editor.util.color.ColorTheme;

import java.awt.*;

public class CommonColors {
    public static final ColorTheme DEFAULT = new ColorTheme(
            "default",
            new Color(156, 156, 156),      // control
            new Color(139, 139, 139),      // checkerboard - light
            new Color(126, 126, 126),      // checkerboard - dark
            new Color(70,  70,  70),       // popup
            new Color(70,  70,  70, 70) // popup shadow
    );

    public static final ColorTheme HALLOWEEN = new ColorTheme(
            "halloween",
            new Color(120, 65,  168),
            new Color(119, 77,  187),
            new Color(101, 69,  161),
            new Color(55,  26,  96),
            new Color(55,  26,  96, 70)
    );
}
