package org.seasle;

public class Utils {
    public static int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }
}