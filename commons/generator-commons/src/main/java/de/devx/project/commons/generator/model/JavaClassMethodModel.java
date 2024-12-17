package de.devx.project.commons.generator.model;

import de.devx.project.commons.generator.type.JavaAccessModifierType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class JavaClassMethodModel {

    private final String name;
    private final Set<JavaAccessModifierType> accessModifiers;
    private final List<JavaTypeArgumentModel> typeArguments;
    private final JavaTypeModel returnType;
    private final List<JavaClassMethodParameterModel> parameters;

    public boolean isStatic() {
        return accessModifiers.contains(JavaAccessModifierType.STATIC);
    }

    public boolean isFinal() {
        return accessModifiers.contains(JavaAccessModifierType.FINAL);
    }

    public boolean isPrivate() {
        return accessModifiers.contains(JavaAccessModifierType.PRIVATE);
    }

    public boolean isProtected() {
        return accessModifiers.contains(JavaAccessModifierType.PROTECTED);
    }

    public boolean isPublic() {
        return accessModifiers.contains(JavaAccessModifierType.PUBLIC);
    }

    public int getParameterCount() {
        return parameters.size();
    }
}
