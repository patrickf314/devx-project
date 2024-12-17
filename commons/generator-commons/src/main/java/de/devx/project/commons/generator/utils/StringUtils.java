package de.devx.project.commons.generator.utils;

public final class StringUtils {

    private StringUtils() {
        // No instances
    }

    public static String capitalize(String string) {
        if(string == null || string.isEmpty()) {
            return string;
        }

        var c = string.charAt(0);
        return Character.toUpperCase(c) + string.substring(1);
    }
}
