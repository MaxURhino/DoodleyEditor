package net.maxurhino.doodley_editor.util.render.font;

import net.maxurhino.doodley_editor.util.interfaces.Destroyable;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.*;

import java.io.IOException;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.freetype.FreeType.*;

public class Font implements Destroyable {
    private final Map<Character, Glyph> glyphs = new HashMap<>();

    private long library;
    private FT_Face face;

    public final int size;
    private int unitsPerEm;

    private final String path;

    public Font(Path path, int size) {
        this(path.toAbsolutePath().toString(), size);
    }

    public Font(String path, int size) {
        this.path = path;
        this.size = size;
        load(path);
    }

    private ByteBuffer fontBuffer;

    private void load(String path) {
        try {
            Path filePath = Path.of(path);
            fontBuffer = BufferUtils.createByteBuffer((int) Files.size(filePath));
            fontBuffer.put(Files.readAllBytes(filePath));
            fontBuffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer libraryPtr = stack.mallocPointer(1);
            if (FT_Init_FreeType(libraryPtr) != 0) {
                throw new RuntimeException("Failed to initialize library");
            }
            library = libraryPtr.get(0);

            PointerBuffer facePtr = stack.mallocPointer(1);
            if (FT_New_Memory_Face(library, fontBuffer, 0, facePtr) != 0) {
                throw new RuntimeException("Failed to initialize face");
            }
            face = FT_Face.create(facePtr.get(0));

            this.unitsPerEm = face.units_per_EM();

            loadGlyphs();
        }
    }

    public float getScale() {
        return (float) size / unitsPerEm;
    }

    public float getTextWidth(String text) {
        return TextRenderer.getTextWidth(this, text);
    }

    public int getLineHeight() {
        return size;
    }

    private void loadGlyphs() {
        for (char c = 32; c < 127; c++) {
            if (FT_Load_Char(face, c, FT_LOAD_NO_SCALE) != 0) {
                continue;
            }

            FT_GlyphSlot slot = face.glyph();
            assert slot != null;

            List<List<Vector2f>> contours = decomposeOutline(slot.outline());

            Glyph glyph = new Glyph(
                    contours,
                    slot.advance().x(),
                    slot.metrics().horiBearingX(),
                    slot.metrics().horiBearingY(),
                    slot.metrics().width(),
                    slot.metrics().height()
            );

            glyphs.put(c, glyph);
        }
    }

    private static List<List<Vector2f>> decomposeOutline(FT_Outline outline) {
        OutlineBuilder builder = new OutlineBuilder();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FT_Outline_Funcs funcs = FT_Outline_Funcs.calloc(stack);

            FT_Outline_MoveToFuncI moveTo = (to, _) -> {
                FT_Vector v = FT_Vector.create(to);
                builder.moveTo(v.x(), v.y());
                return 0;
            };
            FT_Outline_LineToFuncI lineTo = (to, _) -> {
                FT_Vector v = FT_Vector.create(to);
                builder.lineTo(v.x(), v.y());
                return 0;
            };
            FT_Outline_ConicToFuncI conicTo = (control, to, _) -> {
                FT_Vector c = FT_Vector.create(control);
                FT_Vector v = FT_Vector.create(to);
                builder.conicTo(c.x(), c.y(), v.x(), v.y());
                return 0;
            };
            FT_Outline_CubicToFuncI cubicTo = (control1, control2, to, _) -> {
                FT_Vector c1 = FT_Vector.create(control1);
                FT_Vector c2 = FT_Vector.create(control2);
                FT_Vector v = FT_Vector.create(to);
                builder.cubicTo(c1.x(), c1.y(), c2.x(), c2.y(), v.x(), v.y());
                return 0;
            };

            funcs.move_to(moveTo);
            funcs.line_to(lineTo);
            funcs.conic_to(conicTo);
            funcs.cubic_to(cubicTo);

            FT_Outline_Decompose(outline, funcs, NULL);

            Reference.reachabilityFence(moveTo);
            Reference.reachabilityFence(lineTo);
            Reference.reachabilityFence(conicTo);
            Reference.reachabilityFence(cubicTo);
        }

        return builder.contours;
    }

    private static class OutlineBuilder {
        final List<List<Vector2f>> contours = new ArrayList<>();
        List<Vector2f> current;

        void moveTo(float x, float y) {
            current = new ArrayList<>();
            contours.add(current);
            current.add(new Vector2f(x, y));
        }

        void lineTo(float x, float y) {
            current.add(new Vector2f(x, y));
        }

        void conicTo(float cx, float cy, float x, float y) {
            Vector2f p0 = current.getLast();
            int segments = 12;
            for (int i = 1; i <= segments; i++) {
                float t = i / (float) segments;
                float mt = 1 - t;
                current.add(new Vector2f(
                        mt * mt * p0.x + 2 * mt * t * cx + t * t * x,
                        mt * mt * p0.y + 2 * mt * t * cy + t * t * y
                ));
            }
        }

        void cubicTo(float c1x, float c1y, float c2x, float c2y, float x, float y) {
            Vector2f p0 = current.getLast();
            int segments = 16;
            for (int i = 1; i <= segments; i++) {
                float t = i / (float) segments;
                float mt = 1 - t;
                current.add(new Vector2f(
                        mt*mt*mt*p0.x + 3*mt*mt*t*c1x + 3*mt*t*t*c2x + t*t*t*x,
                        mt*mt*mt*p0.y + 3*mt*mt*t*c1y + 3*mt*t*t*c2y + t*t*t*y
                ));
            }
        }
    }

    public Glyph getGlyph(char c) {
        return glyphs.get(c);
    }

    public Font setSize(int size) {
        return new Font(path, size);
    }

    @Override
    public void destroy() {
        FT_Done_Face(face);
        FT_Done_FreeType(library);
        fontBuffer = null;
    }
}