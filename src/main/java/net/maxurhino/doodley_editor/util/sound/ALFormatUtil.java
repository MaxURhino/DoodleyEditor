package net.maxurhino.doodley_editor.util.sound;

import static org.lwjgl.openal.AL10.*;

public class ALFormatUtil {
    public static int getChannels(int format) {
        return switch (format) {
            case AL_FORMAT_MONO8, AL_FORMAT_MONO16 -> 1;
            case AL_FORMAT_STEREO8, AL_FORMAT_STEREO16 -> 2;
            default -> throw new IllegalArgumentException("Unknown AL format: " + format);
        };
    }

    public static int getBytesPerSample(int format) {
        return switch (format) {
            case AL_FORMAT_MONO8, AL_FORMAT_STEREO8 -> 1;
            case AL_FORMAT_MONO16, AL_FORMAT_STEREO16 -> 2;
            default -> throw new IllegalArgumentException("Unknown AL format: " + format);
        };
    }
}
