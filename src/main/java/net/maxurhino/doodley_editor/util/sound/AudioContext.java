package net.maxurhino.doodley_editor.util.sound;

import net.maxurhino.doodley_editor.util.interfaces.Destroyable;

import org.lwjgl.openal.*;

import java.nio.*;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AudioContext implements Destroyable {
    private long device;
    private long context;

    public void create() {
        this.device = alcOpenDevice((ByteBuffer) null);
        if (this.device == NULL) {
            throw new RuntimeException("Failed to open default OpenAL device");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(this.device);

        this.context = alcCreateContext(this.device, (IntBuffer) null);
        if (this.context == NULL) {
            throw new RuntimeException("Failed to create OpenAL context");
        }

        alcMakeContextCurrent(this.context);
        AL.createCapabilities(deviceCaps);
    }

    @Override
    public void destroy() {
        alcMakeContextCurrent(NULL);
        alcDestroyContext(this.context);
        alcCloseDevice(this.device);
    }
}
