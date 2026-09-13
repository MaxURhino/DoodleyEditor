package net.maxurhino.doodley_editor.util.sound.decoders;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class OGGDecoder extends Decoder {
    public OGGDecoder() {}

    public DecodedAudio decode(byte[] oggBytes) {
        ByteBuffer encoded = MemoryUtil.memAlloc(oggBytes.length);
        encoded.put(oggBytes).flip();

        try (MemoryStack stack = stackPush()) {
            IntBuffer channelsBuf = stack.mallocInt(1);
            IntBuffer sampleRateBuf = stack.mallocInt(1);
            PointerBuffer outputPtr = stack.mallocPointer(1);

            int samplesPerChannel = stb_vorbis_decode_memory(encoded, channelsBuf, sampleRateBuf, outputPtr);
            if (samplesPerChannel < 0) {
                throw new RuntimeException("Failed to decode OGG data (stb_vorbis error code " + samplesPerChannel + ")");
            }

            int channels = channelsBuf.get(0);
            int sampleRate = sampleRateBuf.get(0);

            int totalSamples = samplesPerChannel * channels;
            ShortBuffer decodedPcm = MemoryUtil.memShortBuffer(outputPtr.get(0), totalSamples);

            ByteBuffer pcm = ByteBuffer.allocateDirect(totalSamples * 2)
                    .order(ByteOrder.nativeOrder());
            pcm.asShortBuffer().put(decodedPcm);
            pcm.rewind();

            LibCStdlib.free(outputPtr);

            int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

            return new DecodedAudio(pcm, format, sampleRate);
        } finally {
            MemoryUtil.memFree(encoded);
        }
    }
}
