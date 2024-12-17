package de.devx.project.commons.generator.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class JavaClassModel {

    private final String packageName;
    private final String name;
    private final List<JavaTypeArgumentModel> typeArguments;
    private final JavaTypeModel superClass;

    private final List<JavaClassFieldModel> fields;
    private final List<JavaClassMethodModel> methods;
    private final List<JavaAnnotationModel> annotations;

    private final boolean javaRecord;

    public JavaClassModel(String packageName, String name, List<JavaClassFieldModel> fields, List<JavaClassMethodModel> methods) {
        this(packageName, name, (JavaTypeModel) null, fields, methods);
    }

    public JavaClassModel(String packageName, String name, JavaTypeModel superClass, List<JavaClassFieldModel> fields, List<JavaClassMethodModel> methods) {
        this(packageName, name, Collections.emptyList(), superClass, fields, methods);
    }

    public JavaClassModel(String packageName, String name, List<JavaTypeArgumentModel> typeArguments, List<JavaClassFieldModel> fields, List<JavaClassMethodModel> methods) {
        this(packageName, name, typeArguments, null, fields, methods);
    }

    public JavaClassModel(String packageName, String name, List<JavaTypeArgumentModel> typeArguments, JavaTypeModel superClass, List<JavaClassFieldModel> fields, List<JavaClassMethodModel> methods) {
        this(packageName, name, typeArguments, superClass, fields, methods, Collections.emptyList(), false);
    }

    public Optional<JavaTypeModel> getSuperClass() {
        return Optional.ofNullable(superClass);
    }

    public String getFullyQualifiedName() {
        return packageName + "." + name;
    }

    public String getDeclaration() {
        if (typeArguments.isEmpty()) {
            return name;
        }

        return name + "<" + typeArguments.stream().map(JavaTypeArgumentModel::getDefinition).collect(Collectors.joining(", ")) + ">";
    }

    public JavaTypeModel asType() {
        return JavaTypeModel.objectType(packageName, name, typeArguments.stream()
                .map(JavaTypeArgumentModel::asType)
                .toList());
    }

    public boolean isAnnotationPresent(String fullyQualifiedName) {
        return annotations.stream().anyMatch(annotation -> fullyQualifiedName.equals(annotation.getType().getQualifiedTypeName()));
    }
}
