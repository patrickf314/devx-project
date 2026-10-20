package de.devx.project.assertj.assertion.gennerator;

import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import de.devx.project.commons.generator.model.JavaTypeModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.devx.project.commons.generator.utils.ImportUtils.asJavaImport;

/**
 * Decides which types of a generated java file can be imported and referenced
 * by their simple name and which ones have to be referenced by their fully qualified name,
 * because their simple name clashes with another type of the file
 * (e.g. {@code a.SectionDTO} and {@code b.SectionDTO}).
 */
public class JavaTypeNameResolver {

    private final String packageName;

    /**
     * Simple name to fully qualified name of all types that are referenced by their simple name
     */
    private final Map<String, String> simpleNames = new HashMap<>();

    /**
     * @param packageName   the package of the generated file
     * @param className     the simple name of the generated class
     * @param requiredTypes fully qualified names of types which are always referenced by their simple name
     * @param optionalTypes fully qualified names of types which are referenced by their simple name if it is unique,
     *                      by their fully qualified name otherwise
     */
    public JavaTypeNameResolver(String packageName, String className, Collection<String> requiredTypes, Collection<String> optionalTypes) {
        this.packageName = packageName;

        simpleNames.put(className, packageName + "." + className);
        requiredTypes.forEach(type -> simpleNames.putIfAbsent(simpleName(type), type));

        var optionalTypesBySimpleName = optionalTypes.stream()
                .collect(Collectors.groupingBy(JavaTypeNameResolver::simpleName, Collectors.toSet()));
        optionalTypesBySimpleName.forEach((simpleName, types) -> {
            if (types.size() == 1 && !simpleNames.containsKey(simpleName)) {
                simpleNames.put(simpleName, types.iterator().next());
            }
        });
    }

    /**
     * Collects the fully qualified names of all object types referenced by the given type,
     * including type arguments and constraints.
     */
    public static Stream<String> streamQualifiedNames(JavaTypeModel type) {
        return streamQualifiedNames(type, new HashSet<>());
    }

    private static Stream<String> streamQualifiedNames(JavaTypeModel type, Set<JavaTypeModel> visited) {
        if (!visited.add(type)) {
            return Stream.empty();
        }

        var self = type.getPackageName().isPresent() && !type.isPrimitive()
                ? Stream.of(type.getPackageName().get() + "." + type.getName())
                : Stream.<String>empty();

        return Stream.concat(
                self,
                Stream.concat(
                        type.getTypeArguments().stream().flatMap(t -> streamQualifiedNames(t, visited)),
                        type.getTypeConstraint().stream().flatMap(t -> streamQualifiedNames(t, visited))
                )
        );
    }

    public List<String> getImports() {
        return simpleNames.values()
                .stream()
                .flatMap(type -> asJavaImport(packageName, packageOf(type), simpleName(type)).stream())
                .sorted()
                .toList();
    }

    public String type(JavaTypeModel type) {
        return type(type, false);
    }

    public String typeArgument(JavaTypeArgumentModel typeArgument) {
        return typeArgument.getTypeConstraint()
                .map(constraint -> typeArgument.getName() + " extends " + type(constraint, true))
                .orElse(typeArgument.getName());
    }

    public String className(JavaClassModel classModel) {
        return name(classModel.getPackageName(), classModel.getName());
    }

    private String type(JavaTypeModel type, boolean nonPrimitive) {
        if (type.isWildcard()) {
            return "?";
        }

        if (type.isPrimitive() && !type.isArray()) {
            return nonPrimitive ? type.getClassName().orElseThrow() : type.getName();
        }

        var builder = new StringBuilder(type.getPackageName()
                .filter(p -> !type.isPrimitive())
                .map(p -> name(p, type.getName()))
                .orElse(type.getName()));

        if (!type.getTypeArguments().isEmpty()) {
            builder.append("<")
                    .append(type.getTypeArguments().stream()
                            .map(t -> type(t, true))
                            .collect(Collectors.joining(", ")))
                    .append(">");
        }

        if (type.isArray()) {
            builder.append("[]");
        }

        return builder.toString();
    }

    private String name(String packageName, String name) {
        var qualifiedName = packageName + "." + name;
        return qualifiedName.equals(simpleNames.get(simpleName(qualifiedName))) ? name : qualifiedName;
    }

    private static String simpleName(String qualifiedName) {
        return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    }

    private static String packageOf(String qualifiedName) {
        return qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
    }
}
