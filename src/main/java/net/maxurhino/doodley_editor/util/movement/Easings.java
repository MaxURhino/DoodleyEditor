package net.maxurhino.doodley_editor.util.movement;

public class Easings {
    public static double easeOutCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    public static double easeInCubic(double x) {
        return Math.pow(x, 3);
    }
}
