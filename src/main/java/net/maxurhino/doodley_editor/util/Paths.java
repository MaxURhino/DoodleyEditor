package net.maxurhino.doodley_editor.util;

import net.maxurhino.doodley_editor.Main;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class Paths {
    public static Path getPath(String file) {
        try {
            return Main.getResourcePath(file);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path resource(String file) {
        return getPath("Resources/" + file);
    }

    public static Path font(String file) {
        return resource("Fonts/" + file + ".ttf");
    }

    public static Path font(String family, String file) {
        return resource("Fonts/" + family + "/" + family + file + ".ttf");
    }

    public static Path music(String file) {
        return resource("Music/" + file);
    }

    public static Path music(String author, String album, String songFile) {
        return music(author + "/" + album + "/" + songFile);
    }

    public static class image {
        public static Path plain(String path) {
            return resource("Images/" + path);
        }

        public static Path PNG(String path) {
            return plain("PNG/" + path + ".png");
        }

        public static Path SVG(String path) {
            return plain("SVG/" + path + ".svg");
        }
    }
}
