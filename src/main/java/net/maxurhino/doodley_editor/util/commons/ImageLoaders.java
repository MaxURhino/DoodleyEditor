package net.maxurhino.doodley_editor.util.commons;

import net.maxurhino.doodley_editor.util.render.texture.loaders.*;
import net.maxurhino.doodley_editor.util.render.texture.loaders.types.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ImageLoaders {
    public static final Map<String, Supplier<? extends Loader>> loaders = new LinkedHashMap<>() {{
        put("png", PNGLoader::new);
        put("svg", SVGLoader::new);
        put("jpg", JPGLoader::new);
    }};
}
