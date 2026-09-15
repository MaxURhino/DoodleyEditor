package net.maxurhino.doodley_editor.util.render.shader;

public class WaveShader extends Shader {
    private static final String VERTEX_SOURCE = """
            #version 120

            uniform float uTime;
            uniform float uAmplitude;
            uniform float uFrequency;
            uniform float uSpeed;

            void main() {
                vec4 pos = gl_Vertex;

                pos.y += sin(pos.x * uFrequency + uTime * uSpeed) * uAmplitude;

                gl_Position = gl_ModelViewProjectionMatrix * pos;
                gl_FrontColor = gl_Color;
                gl_TexCoord[0] = gl_MultiTexCoord0;
            }
            """;

    private static final String FRAGMENT_SOURCE = """
            #version 120

            uniform bool uUseTexture;
            uniform sampler2D uTexture;

            void main() {
                gl_FragColor = uUseTexture
                        ? texture2D(uTexture, gl_TexCoord[0].st) * gl_Color
                        : gl_Color;
            }
            """;

    public WaveShader() {
        super(VERTEX_SOURCE, FRAGMENT_SOURCE);
        use();
        setAmplitude(6f);
        setFrequency(0.05f);
        setSpeed(1f);
        setUseTexture(false);
        stop();
    }

    public void setTime(float time) {
        setUniform1f("uTime", time);
    }

    public void setAmplitude(float amplitude) {
        setUniform1f("uAmplitude", amplitude);
    }

    public void setFrequency(float frequency) {
        setUniform1f("uFrequency", frequency);
    }

    public void setSpeed(float speed) {
        setUniform1f("uSpeed", speed);
    }

    public void setUseTexture(boolean useTexture) {
        setUniform1i("uUseTexture", useTexture ? 1 : 0);
    }
}