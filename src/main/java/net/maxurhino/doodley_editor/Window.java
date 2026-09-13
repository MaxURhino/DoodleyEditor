package net.maxurhino.doodley_editor;

import net.maxurhino.doodley_editor.util.interfaces.Destroyable;

import net.maxurhino.doodley_editor.util.platform_specific.windows.*;
import org.joml.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window implements Destroyable {
    public final Vector2i setupSize;
    public Vector2i currentSize;
    private String title;
    private long handle;

    private double lastFrameTime;
    private float deltaTime;

    public Window(Vector2i size, String title) {
        this.setupSize = size;
        this.currentSize = size;
        this.title = title;
    }

    public Window(int width, int height, String title) {
        this(new Vector2i(width, height), title);
    }

    private Runnable onFrame;

    public void setOnFrame(Runnable onFrame) {
        this.onFrame = onFrame;
    }

    public void create() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();

        this.handle = glfwCreateWindow(this.setupSize.x, this.setupSize.y, this.title, NULL, NULL);
        if (this.handle == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode == null) {
            throw new RuntimeException("Failed to retrieve video mode");
        }

        glfwSetWindowPos(
                this.handle,
                (vidmode.width() - this.setupSize.x) / 2,
                (vidmode.height() - this.setupSize.y) / 2
        );

        glfwMakeContextCurrent(this.handle);
        glfwSwapInterval(1); // vsync
        glfwShowWindow(this.handle);
        GL.createCapabilities();

        if (Platform.get() == Platform.WINDOWS) {
            long hwnd = GLFWNativeWin32.glfwGetWin32Window(this.handle);
            WindowsDarkTitleBar.enable(hwnd, true);
        }

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, this.setupSize.x, this.setupSize.y, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        glfwSetKeyCallback(this.handle, (_, k, _, a, _) -> {
            if (k == GLFW_KEY_ESCAPE && a == GLFW_RELEASE) {
                glfwSetWindowShouldClose(this.handle, true);
            }
        });

        glfwSetWindowRefreshCallback(this.handle, _ -> {
            if (this.onFrame != null) {
                this.onFrame.run();
            }
        });

        glfwSetFramebufferSizeCallback(this.handle, (_, w, h) -> {
            this.currentSize = new Vector2i(w, h);

            glViewport(0, 0, w, h);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, this.currentSize.x, this.currentSize.y, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);
        });

        this.lastFrameTime = glfwGetTime();
    }

    public Vector2f getWindowScaleFromInitial() {
        return new Vector2f(this.currentSize).div(new Vector2f(this.setupSize));
    }

    public Vector2f getScaleFactor() {
        Vector2f windowScaleFromInitial = getWindowScaleFromInitial();
        float temp1 = windowScaleFromInitial.x;
        float temp2 = windowScaleFromInitial.y;
        if (windowScaleFromInitial.y > windowScaleFromInitial.x) {
            windowScaleFromInitial.x = temp2;
        } else {
            windowScaleFromInitial.y = temp1;
        }
        return windowScaleFromInitial;
    }

    public Vector2f getScaledSize() {
        return new Vector2f(this.currentSize).div(getScaleFactor());
    }

    public Window setTitle(String title) {
        this.title = title;
        glfwSetWindowTitle(this.handle, title);
        return this;
    }

    public boolean running() {
        return !glfwWindowShouldClose(this.handle);
    }

    public void poll() {
        glfwSwapBuffers(this.handle);
        glfwPollEvents();

        double now = glfwGetTime();
        this.deltaTime = (float) (now - this.lastFrameTime);
        this.lastFrameTime = now;
    }

    public float getDeltaTime() {
        return this.deltaTime;
    }

    public void destroy() {
        glfwFreeCallbacks(this.handle);
        glfwDestroyWindow(this.handle);

        glfwTerminate();
    }

    public long getHandle() {
        return handle;
    }
}
