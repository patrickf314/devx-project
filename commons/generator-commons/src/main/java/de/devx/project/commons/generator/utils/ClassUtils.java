package de.devx.project.commons.generator.utils;

import java.util.Map;

public final class ClassUtils {

    private static final Map<String, Class<?>> BOXED_TYPES = Map.of(
            "byte", Byte.class,
            "short", Short.class,
            "char", Character.class,
            "int", Integer.class,
            "long", Long.class,
            "boolean", Boolean.class,
            "float", Float.class,
            "double", Double.class
    );

    public static String extractPackageName(String fullyQualifiedClassName) {
        var i = fullyQualifiedClassName.lastIndexOf('.');
        if (i == -1 || i == 0 || i == fullyQualifiedClassName.length() - 1) {
            throw new IllegalArgumentException("String " + fullyQualifiedClassName + " is not a valid fully qualified class name");
        }
        return fullyQualifiedClassName.substring(0, i);
    }

    public static String extractSimpleClassName(String fullyQualifiedClassName) {
        var i = fullyQualifiedClassName.lastIndexOf('.');
        if (i == -1 || i == 0 || i == fullyQualifiedClassName.length() - 1) {
            throw new IllegalArgumentException("String " + fullyQualifiedClassName + " is not a valid fully qualified class name");
        }
        return fullyQualifiedClassName.substring(i + 1);
    }

    public static Class<?> toBoxedType(String primitiveName) {
        return BOXED_TYPES.get(primitiveName);
    }
}
