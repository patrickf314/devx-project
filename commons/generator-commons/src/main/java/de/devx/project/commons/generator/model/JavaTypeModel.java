package de.devx.project.commons.generator.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.devx.project.commons.generator.utils.ImportUtils.asJavaImport;
import static java.util.Collections.emptyList;

/**
 * Model representing a java type
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaTypeModel {

    private static final String JAVA_LANG_PACKAGE = "java.lang";
    public static final JavaTypeModel VOID = primitiveType("void", "Void");
    public static final JavaTypeModel WILDCARD = new JavaTypeModel(null, "?", null, emptyList(), false);
    public static final JavaTypeModel BOOLEAN = primitiveType("boolean", "Boolean");

    /**
     * The name of the package of this type,
     * null for a generic template type.
     */
    private final String packageName;
    /**
     * The name of this type, should be equal to the class name,
     * except for primitive and generic template types. For those, the name is the name
     * of the primitive type (e.g. 'int') or generic template (e.g. 'T') and the class name is 'Integer' or null
     * respectively.
     */
    private final String name;
    /**
     * The simple name of the java class representing this type,
     * null of a generic template type
     */
    private final String className;
    /**
     * A list of generics
     */
    private final List<JavaTypeModel> typeArguments;
    /**
     * A flag indicating if this type is an array
     */
    private final boolean array;

    public static JavaTypeModel primitiveType(String name, String className) {
        return new JavaTypeModel(JAVA_LANG_PACKAGE, name, className, emptyList(), false);
    }

    public static JavaTypeModel arrayType(JavaTypeModel componentType) {
        return new JavaTypeModel(componentType.packageName, componentType.name, componentType.className, componentType.typeArguments, true);
    }

    public static JavaTypeModel objectType(String packageName, String className) {
        return objectType(packageName, className, emptyList());
    }

    public static JavaTypeModel objectType(String packageName, String className, List<JavaTypeModel> typeArguments) {
        return new JavaTypeModel(packageName, className, className, typeArguments, false);
    }

    public static JavaTypeModel genericTemplateType(String name) {
        return new JavaTypeModel(null, name, null, emptyList(), false);
    }

    public Optional<String> getPackageName() {
        return Optional.ofNullable(packageName);
    }

    public Optional<String> getClassName() {
        return Optional.ofNullable(className);
    }

    public String getQualifiedTypeName() {
        return getQualifiedTypeName(false);
    }

    public String getNonPrimitiveQualifiedTypeName() {
        return getQualifiedTypeName(true);
    }

    private String getQualifiedTypeName(boolean usePrimitiveClassName) {
        if(isWildcard()) {
            return "?";
        }

        if (isPrimitive() && !array) {
            return usePrimitiveClassName ? className : name;
        }

        var builder = new StringBuilder(name);
        if (!typeArguments.isEmpty()) {
            builder.append("<")
                    .append(typeArguments.stream()
                            .map(JavaTypeModel::getNonPrimitiveQualifiedTypeName)
                            .collect(Collectors.joining(", ")))
                    .append(">");
        }

        if (array) {
            builder.append("[]");
        }

        return builder.toString();
    }

    public boolean isWildcard() {
        return packageName == null && className == null;
    }

    public boolean isPrimitive() {
        return JAVA_LANG_PACKAGE.equals(packageName) && className != null && !className.equals(name);
    }

    public Stream<String> streamImports(String currentPackageName) {
        if (packageName == null) {
            return Stream.empty();
        }

        var i = name.indexOf('$');
        var rootClassName = i == -1 ? name : name.substring(0, i);
        return Stream.concat(
                asJavaImport(currentPackageName, packageName, rootClassName).stream(),
                typeArguments.stream().flatMap(type -> type.streamImports(currentPackageName))
        );
    }
}
