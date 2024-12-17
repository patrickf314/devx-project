package de.devx.project.commons.generator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A model representing a generic type definition
 * like
 * {@code T} or {@code T extends Number}
 */
@Data
@RequiredArgsConstructor
public class JavaTypeArgumentModel {

    private final String name;
    private final JavaTypeModel typeConstraint;

    public JavaTypeArgumentModel(String name) {
        this(name, null);
    }

    public String getDefinition() {
        if (typeConstraint == null) {
            return name;
        } else {
            return name + " extends " + typeConstraint.getNonPrimitiveQualifiedTypeName();
        }
    }

    public Optional<JavaTypeModel> getTypeConstraint() {
        return Optional.ofNullable(typeConstraint);
    }

    public Stream<String> streamImports(String currentPackageName) {
        return getTypeConstraint().stream()
                .flatMap(type -> type.streamImports(currentPackageName));
    }

    public JavaTypeModel asType() {
        return JavaTypeModel.genericTemplateType(name);
    }
}
