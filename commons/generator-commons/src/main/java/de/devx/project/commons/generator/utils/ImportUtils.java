package de.devx.project.commons.generator.utils;

import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.generator.model.JavaTypeModel;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ImportUtils {

    private static final String JAVA_LANG_PACKAGE = "java.lang";

    private ImportUtils() {
        // No instances
    }

    public static Optional<String> asJavaImport(String currentPackageName, JavaClassModel classModel) {
        return asJavaImport(currentPackageName, classModel.asType());
    }

    public static Optional<String> asJavaImport(String currentPackageName, JavaTypeModel typeModel) {
        return typeModel.getPackageName()
                .flatMap(packageName -> asJavaImport(currentPackageName, packageName, typeModel.getName()));
    }

    public static Optional<String> asJavaImport(String currentPackageName, String packageName, String className) {
        if (currentPackageName.equals(packageName) || JAVA_LANG_PACKAGE.equals(packageName)) {
            return Optional.empty();
        }

        return Optional.of(packageName + "." + className);
    }

    public static List<String> combineJavaImports(Stream<String> a, Stream<String> b) {
        return Stream.concat(a, b)
                .distinct()
                .sorted()
                .toList();
    }

    @SafeVarargs
    public static List<String> combineJavaImports(Stream<String>... streams) {
        return Arrays.stream(streams)
                .flatMap(Function.identity())
                .distinct()
                .sorted()
                .toList();
    }
}
