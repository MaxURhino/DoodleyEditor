package net.maxurhino.doodley_editor.util.render.shader;

import net.maxurhino.doodley_editor.util.interfaces.Destroyable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;


public class Shader implements Destroyable {
    private final int programId;
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    public Shader(String vertexSource, String fragmentSource) {
        int vertexId = compile(GL_VERTEX_SHADER, vertexSource);
        int fragmentId = compile(GL_FRAGMENT_SHADER, fragmentSource);

        this.programId = glCreateProgram();
        glAttachShader(this.programId, vertexId);
        glAttachShader(this.programId, fragmentId);
        glLinkProgram(this.programId);

        if (glGetProgrami(this.programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to link shader program: " + glGetProgramInfoLog(this.programId));
        }

        glDetachShader(this.programId, vertexId);
        glDetachShader(this.programId, fragmentId);
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);
    }
    public Shader(Path vertexPath, Path fragmentPath) {
        this(readSource(vertexPath), readSource(fragmentPath));
    }

    private static String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader source: " + path, e);
        }
    }

    private static int compile(int type, String source) {
        int id = glCreateShader(type);
        glShaderSource(id, source);
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(id);
            glDeleteShader(id);
            String kind = type == GL_VERTEX_SHADER ? "vertex" : "fragment";
            throw new RuntimeException("Failed to compile " + kind + " shader: " + log);
        }

        return id;
    }

    private int location(String name) {
        return uniformLocations.computeIfAbsent(name, n -> glGetUniformLocation(this.programId, n));
    }

    public void use() {
        glUseProgram(this.programId);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void render(Runnable objects) {
        use();
        objects.run();
        stop();
    }

    public void setUniform1i(String name, int value) {
        glUniform1i(location(name), value);
    }

    public void setUniform1f(String name, float value) {
        glUniform1f(location(name), value);
    }

    public void setUniform2f(String name, float x, float y) {
        glUniform2f(location(name), x, y);
    }

    public void setUniform3f(String name, float x, float y, float z) {
        glUniform3f(location(name), x, y, z);
    }

    public void setUniform4f(String name, float x, float y, float z, float w) {
        glUniform4f(location(name), x, y, z, w);
    }

    @Override
    public void destroy() {
        glDeleteProgram(this.programId);
    }

}

