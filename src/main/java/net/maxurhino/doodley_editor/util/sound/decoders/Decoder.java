package net.maxurhino.doodley_editor.util.sound.decoders;

import java.nio.ByteBuffer;

public abstract class Decoder {
    public Decoder() {}

    public abstract DecodedAudio decode(byte[] bytes);

    public static class DecodedAudio {
        public final ByteBuffer pcm;
        public final int format;
        public final int sampleRate;

        DecodedAudio(ByteBuffer pcm, int format, int sampleRate) {
            this.pcm = pcm;
            this.format = format;
            this.sampleRate = sampleRate;
        }
    }
}
