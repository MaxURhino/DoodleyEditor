package net.maxurhino.doodley_editor.util.render.font;

import net.maxurhino.doodley_editor.util.interfaces.Destroyable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class FontFamily implements Destroyable {
    private final Map<String, Font> fonts;
    private final String familyName;
    private final List<String> types;
    private final BiFunction<String, String, Path> fontPath;

    public FontFamily(String familyName, List<String> types, BiFunction<String, String, Path> fontPath, int size) {
        this.familyName = familyName;
        this.types = types;
        this.fontPath = fontPath;

        this.fonts = new HashMap<>();

        for (String type : types) {
            Path path = fontPath.apply(familyName, type);

            this.fonts.put(
                    type,
                    new Font(path, size)
            );
        }
    }

    public FontFamily resize(int size) {
        destroy();
        FontFamily newFamily = getResized(size);
        this.fonts.clear();
        this.fonts.putAll(newFamily.fonts);
        return this;
    }

    public FontFamily getResized(int size) {
        return new FontFamily(this.familyName, this.types, this.fontPath, size);
    }

    public Font getStyle(String type) {
        if (!this.fonts.containsKey(type)) {
            System.err.println("Font with type \"" + type + "\" not found.");
            System.out.println("Here are all the available fonts:" + this.fonts.keySet());
        }

        return this.fonts.get(type);
    }

    @Override
    public void destroy() {
        for (Font font : this.fonts.values()) {
            font.destroy();
        }
    }
}
