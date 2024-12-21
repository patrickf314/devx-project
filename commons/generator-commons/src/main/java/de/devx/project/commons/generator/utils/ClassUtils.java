package de.devx.project.commons.generator.utils;

import java.util.Map;

public final class ClassUtils {

    private static final Map<String, Class<?>> BOXED_TYPES = Map.of(
            "void", Void.class,
            "byte", Byte.class,
            "short", Short.class,
            "char", Character.class,
            "int", Integer.class,
            "long", Long.class,
            "boolean", Boolean.class,
            "float", Float.class,
            "double", Double.class
    );

    public static String extractSimpleClassName(Class<?> c) {
        var packageName = c.getPackage().getName();
        return c.getName().substring(packageName.length() + 1);
    }

    public static Class<?> toBoxedType(String primitiveName) {
        return BOXED_TYPES.get(primitiveName);
    }
}
