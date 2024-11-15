package de.devx.project.commons.processor.utils;

import lombok.RequiredArgsConstructor;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.devx.project.commons.processor.utils.ExecutableElementUtils.areMethodsEqual;
import static de.devx.project.commons.processor.utils.TypeElementUtils.isClass;

@RequiredArgsConstructor
public class AnnotationElementUtils {

    public static boolean isAnnotationPresent(Element element, String annotation) {
        return findAnnotationMirror(element, annotation).isPresent();
    }

    public static Optional<AnnotationMirror> findAnyAnnotationMirror(Element element, Set<String> annotations) {
        return annotations.stream()
                .map(annotation -> findAnnotationMirror(element, annotation))
                .flatMap(Optional::stream)
                .findAny();
    }

    public static Optional<AnnotationMirror> findAnnotationMirror(Element element, Class<?> annotation) {
        return findAnnotationMirror(element, annotation.getName());
    }

    public static Optional<AnnotationMirror> findAnnotationMirror(Element element, TypeElement annotation) {
        return findAnnotationMirror(element, annotation.getQualifiedName().toString());
    }

    public static Optional<AnnotationMirror> findAnnotationMirror(Element element, String annotationName) {
        var annotation = element.getAnnotationMirrors()
                .stream()
                .filter(mirror -> isClass(mirror.getAnnotationType(), annotationName))
                .findAny()
                .map(AnnotationMirror.class::cast);

        if (annotation.isPresent()) {
            return annotation;
        }

        if (element instanceof TypeElement classElement) {
            return streamParentTypes(classElement)
                    .map(parentType -> findAnnotationMirror(parentType.asElement(), annotationName))
                    .flatMap(Optional::stream)
                    .findAny();
        }

        if (element instanceof ExecutableElement methodElement && methodElement.getEnclosingElement() instanceof TypeElement classElement) {
            return streamParentTypes(classElement)
                    .map(parentType -> findAnnotationOfMethodInType(methodElement, parentType, annotationName))
                    .flatMap(Optional::stream)
                    .findAny();
        }

        if (element instanceof VariableElement variableElement && element.getEnclosingElement() instanceof ExecutableElement methodElement && methodElement.getEnclosingElement() instanceof TypeElement classElement) {
            return streamParentTypes(classElement)
                    .map(parentType -> findAnnotationOfMethodVariableIn(variableElement, methodElement, parentType, annotationName))
                    .flatMap(Optional::stream)
                    .findAny();
        }

        return Optional.empty();
    }

    private static Optional<AnnotationMirror> findAnnotationOfMethodVariableIn(VariableElement targetVariable, ExecutableElement targetMethod, DeclaredType type, String annotation) {
        var sourceMethod = findSourceMethod(targetMethod, type).orElse(null);
        if (sourceMethod == null) {
            return Optional.empty();
        }

        var parameterIndex = targetMethod.getParameters().indexOf(targetVariable);
        var sourceParameter = sourceMethod.getParameters().get(parameterIndex);
        return findAnnotationMirror(sourceParameter, annotation);
    }

    private static Optional<AnnotationMirror> findAnnotationOfMethodInType(ExecutableElement targetMethod, DeclaredType type, String annotation) {
        return findSourceMethod(targetMethod, type).flatMap(sourceMethod -> findAnnotationMirror(sourceMethod, annotation));
    }

    public static Optional<ExecutableElement> findSourceMethod(ExecutableElement targetMethod, DeclaredType type) {
        return type.asElement()
                .getEnclosedElements()
                .stream()
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(sourceMethod -> areMethodsEqual(targetMethod, sourceMethod))
                .findAny();
    }

    public static Map<String, AnnotationValue> extractFieldsFromAnnotationMirror(AnnotationMirror annotationMirror) {
        return annotationMirror.getElementValues()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getSimpleName().toString(), Map.Entry::getValue));
    }

    public static String getAnnotationName(AnnotationMirror mirror) {
        var type = mirror.getAnnotationType().asElement();
        if (type instanceof TypeElement typeElement) {
            return typeElement.getQualifiedName().toString();
        } else {
            return type.getSimpleName().toString();
        }
    }

    public static Stream<DeclaredType> streamParentTypes(TypeElement classElement) {
        return Stream.concat(
                classElement.getSuperclass() instanceof DeclaredType superClass ? Stream.of(superClass) : Stream.empty(),
                classElement.getInterfaces()
                        .stream()
                        .filter(DeclaredType.class::isInstance)
                        .map(DeclaredType.class::cast)
        );
    }
}
