package net.maxurhino.doodley_editor.util.render.enums;

import static org.lwjgl.opengl.GL12.*;

public enum Filtering {
    BLINEAR(GL_LINEAR),
    LINEAR (GL_LINEAR),
    NEAREST(GL_NEAREST);

    private final int id;

    Filtering(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
