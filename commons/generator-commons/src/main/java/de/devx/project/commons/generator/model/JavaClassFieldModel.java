package de.devx.project.commons.generator.model;

import de.devx.project.commons.generator.type.JavaAccessModifierType;
import lombok.Data;

import java.util.Set;

@Data
public class JavaClassFieldModel {

    private final String name;
    private final JavaTypeModel type;
    private final Set<JavaAccessModifierType> accessModifiers;

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
}
