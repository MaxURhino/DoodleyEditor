package net.maxurhino.doodley_editor;

import net.maxurhino.doodley_editor.app.MainApp;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Main {
    static void main() {
        new MainApp().run();
    }

    public static Path getResourcePath(String path) throws URISyntaxException {
        System.out.printf("Trying to get path of resource: %s; ", path);
        Path returnPath = Paths.get(Objects.requireNonNull(Main.class.getClassLoader().getResource(path)).toURI());
        IO.println("path has gotten successfully.");
        return returnPath;
    }
}
