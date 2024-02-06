package de.devx.project.commons.processor.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public final class ExecutableElementUtils {

    private ExecutableElementUtils() {
        // No instances
    }

    public static boolean areMethodsEqual(ExecutableElement target, ExecutableElement source) {
        return hasSignature(target, source.getSimpleName(), ((ExecutableType) source.asType()).getParameterTypes());
    }

    public static boolean hasSignature(ExecutableElement method, Name name, List<? extends TypeMirror> parameterTypes) {
        if (!method.getSimpleName().equals(name)) {
            return false;
        }

        var methodType = (ExecutableType) method.asType();
        return methodType.getParameterTypes().equals(parameterTypes);
    }

    public static boolean containsMethod(DeclaredType type, ExecutableElement method) {
        return containsMethod(type.asElement(), method);
    }

    public static boolean containsMethod(Element element, ExecutableElement method) {
        return containsMethod(element, method.getSimpleName(), ((ExecutableType) method.asType()).getParameterTypes());
    }

    public static boolean containsMethod(DeclaredType type, Name name, List<? extends TypeMirror> parameterTypes) {
        return containsMethod(type.asElement(), name, parameterTypes);
    }

    public static boolean containsMethod(Element element, Name name, List<? extends TypeMirror> parameterTypes) {
        if (element instanceof TypeElement classElement) {
            return classElement.getEnclosedElements().stream().anyMatch(child -> child instanceof ExecutableElement method && hasSignature(method, name, parameterTypes));
        }

        return false;
    }

}
