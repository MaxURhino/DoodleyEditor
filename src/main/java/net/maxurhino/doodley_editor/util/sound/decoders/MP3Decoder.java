package net.maxurhino.doodley_editor.util.sound.decoders;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.openal.AL10.*;

public class MP3Decoder extends net.maxurhino.doodley_editor.util.sound.decoders.Decoder {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNELS = 2;

    public MP3Decoder() {}

    public DecodedAudio decode(byte[] mp3Bytes) {
        try {
            Bitstream bitstream = new Bitstream(new ByteArrayInputStream(mp3Bytes));
            Decoder decoder = new Decoder();

            ByteArrayOutputStream pcmOut = new ByteArrayOutputStream();
            int channels = -1;
            int sampleRate = -1;

            Header header;
            while ((header = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);

                if (channels == -1) {
                    channels = output.getChannelCount();
                    sampleRate = output.getSampleFrequency();
                }

                short[] samples = output.getBuffer();
                byte[] bytes = new byte[samples.length * 2];
                for (int i = 0; i < samples.length; i++) {
                    bytes[i * 2]     = (byte) (samples[i] & 0xff);
                    bytes[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xff);
                }
                pcmOut.write(bytes);

                bitstream.closeFrame();
            }

            bitstream.close();

            if (channels == -1) {
                throw new RuntimeException("No audio frames decoded — invalid or empty MP3 data");
            }

            byte[] pcmBytes = pcmOut.toByteArray();
            ByteBuffer pcm = memAlloc(pcmBytes.length)
                    .order(ByteOrder.nativeOrder());
            pcm.put(pcmBytes).flip();

            int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

            return new DecodedAudio(pcm, format, sampleRate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode MP3", e);
        }
    }
}
