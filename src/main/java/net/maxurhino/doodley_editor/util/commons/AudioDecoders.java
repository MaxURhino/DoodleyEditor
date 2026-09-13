package net.maxurhino.doodley_editor.util.commons;

import net.maxurhino.doodley_editor.util.sound.decoders.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AudioDecoders {
    public static final Map<String, Supplier<? extends Decoder>> decoders = new LinkedHashMap<>() {{
        put("mp3", MP3Decoder::new);
        put("ogg", OGGDecoder::new);
    }};
}
