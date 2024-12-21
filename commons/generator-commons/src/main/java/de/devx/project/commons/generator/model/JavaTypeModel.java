package de.devx.project.commons.generator.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;
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
     * A flag indicating if this type is an array
     */
    private final boolean array;

    /**
     * A list of generics
     */
    private List<JavaTypeModel> typeArguments = Collections.emptyList();

    /**
     * Constraint of the type, only allowed for generics and wildcards.
     */
    private JavaTypeModel typeConstraint;

    public static JavaTypeModel primitiveType(String name, String className) {
        return new JavaTypeModel(JAVA_LANG_PACKAGE, name, className, false);
    }

    public static JavaTypeModel arrayType(JavaTypeModel componentType) {
        var type = new JavaTypeModel(componentType.packageName, componentType.name, componentType.className, true);
        type.setTypeArguments(componentType.typeArguments);
        return type;
    }

    public static JavaTypeModel objectType(String packageName, String className) {
        return objectType(packageName, className, emptyList());
    }

    public static JavaTypeModel objectType(String packageName, String className, List<JavaTypeModel> typeArguments) {
        var type = new JavaTypeModel(packageName, className, className, false);
        type.setTypeArguments(typeArguments);
        return type;
    }

    public static JavaTypeModel genericTemplateType(String name) {
        return new JavaTypeModel(null, name, null, false);
    }

    public static JavaTypeModel wildcardType() {
        return new JavaTypeModel(null, "?", null, false);
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
        return packageName == null && className == null && "?".equals(name);
    }

    public boolean isPrimitive() {
        return JAVA_LANG_PACKAGE.equals(packageName) && className != null && !className.equals(name);
    }

    public Stream<String> streamImports(String currentPackageName) {
        return streamImports(currentPackageName, new HashSet<>());
    }

    private Stream<String> streamImports(String currentPackageName, Set<JavaTypeModel> containedTypes) {
        if (packageName == null || containedTypes.contains(this)) {
            return Stream.empty();
        }

        containedTypes.add(this);
        return Stream.concat(
                asJavaImport(currentPackageName, packageName, name).stream(),
                Stream.concat(
                        typeArguments.stream().flatMap(type -> type.streamImports(currentPackageName, containedTypes)),
                        getTypeConstraint().stream().flatMap(type -> type.streamImports(currentPackageName, containedTypes))
                )
        );
    }

    public Optional<JavaTypeModel> getTypeConstraint() {
        return Optional.ofNullable(typeConstraint);
    }

    public boolean isObjectType(String packageName, String className) {
        return Objects.equals(this.packageName, packageName) && Objects.equals(this.className, className) && typeArguments.isEmpty();
    }
}
