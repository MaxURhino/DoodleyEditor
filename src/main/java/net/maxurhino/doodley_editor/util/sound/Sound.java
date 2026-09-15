package net.maxurhino.doodley_editor.util.sound;

import net.maxurhino.doodley_editor.util.Paths;
import net.maxurhino.doodley_editor.util.interfaces.Destroyable;

import net.maxurhino.doodley_editor.util.commons.AudioDecoders;
import net.maxurhino.doodley_editor.util.sound.decoders.Decoder;

import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Sound implements Destroyable {
    private int buffer;
    private int source;
    private float duration;

    public Sound(String path) {
        this(java.nio.file.Paths.get(path));
    }

    public Sound(Path path) {
        String fileExtension = Paths.getFileExtension(path);
        IO.println("Getting file extension: " + fileExtension);
        Supplier<? extends Decoder> decoder = AudioDecoders.decoders.get(fileExtension);
        Decoder.DecodedAudio audio;
        try {
            audio = decoder.get().decode(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ByteBuffer pcm = audio.pcm;
        int format = audio.format;
        int sampleRate = audio.sampleRate;

        this.buffer = alGenBuffers();
        alBufferData(this.buffer, format, pcm, sampleRate);
        memFree(pcm);

        this.source = alGenSources();
        alSourcei(this.source, AL_BUFFER, this.buffer);

        int sizeInBytes = alGetBufferi(this.buffer, AL_SIZE);
        int bytesPerSample = 2 * ALFormatUtil.getChannels(format);
        this.duration = (float) sizeInBytes / bytesPerSample / sampleRate;
    }

    public void play()  { alSourcePlay(this.source); }
    public void pause() { alSourcePause(this.source); }
    public void stop()  { alSourceStop(this.source); }

    public void setLooping(boolean loop) {
        alSourcei(this.source, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
    }

    public void setVolume(float volume) {
        alSourcef(this.source, AL_GAIN, volume);
    }

    public void setPitch(float pitch) {
        alSourcef(this.source, AL_PITCH, pitch);
    }

    public boolean isPlaying() {
        return alGetSourcei(this.source, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public float getPlaybackTime() {
        return alGetSourcef(this.source, AL_SEC_OFFSET);
    }

    public float getDuration() {
        return duration;
    }

    public float getPlaybackProgress() {
        return this.duration > 0 ? getPlaybackTime() / this.duration : 0f;
    }

    private Sound setupFromAnother(Sound newSound) {
        this.buffer = newSound.buffer;
        this.source = newSound.source;
        this.duration = newSound.duration;
        return this;
    }

    public Sound load(Path path) {
        this.destroy();
        return setupFromAnother(new Sound(path));
    }

    public Sound load(String path) {
        this.destroy();
        return setupFromAnother(new Sound(path));
    }

    @Override
    public void destroy() {
        alDeleteSources(this.source);
        alDeleteBuffers(this.buffer);
    }
}
