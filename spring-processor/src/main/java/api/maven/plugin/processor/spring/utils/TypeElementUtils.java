package api.maven.plugin.processor.spring.utils;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TypeElementUtils {

    private TypeElementUtils() {
        // No instances
    }

    public static boolean isImplementationOf(TypeMirror typeMirror, Class<?> i) {
        return getInterfaceTypeMirror(typeMirror, i).isPresent();
    }

    public static boolean isImplementationOf(TypeElement typeElement, Class<?> i) {
        return getInterfaceTypeMirror(typeElement, i).isPresent();
    }

    public static Optional<TypeMirror> getInterfaceTypeMirror(TypeMirror typeMirror, Class<?> i) {
        if(!(typeMirror instanceof DeclaredType declaredType) || !(declaredType.asElement() instanceof TypeElement element)) {
            return Optional.empty();
        }

        return getInterfaceTypeMirror(element, i);
    }

    public static Optional<TypeMirror> getInterfaceTypeMirror(TypeElement typeElement, Class<?> i) {
        if (isClass(typeElement, i)) {
            return Optional.of(typeElement.asType());
        }


        var optional = typeElement.getInterfaces()
                .stream()
                .filter(mirror -> isClass(mirror, i))
                .map(TypeMirror.class::cast)
                .findAny();

        if (optional.isPresent()) {
            return optional;
        }

        var superClass = typeElement.getSuperclass();
        if (superClass != null) {
            return getInterfaceTypeMirror(superClass, i);
        }

        return Optional.empty();
    }

    public static List<? extends TypeMirror> getTypeArgumentsOfInterface(TypeMirror typeMirror, Class<?> i) {
        if(!(typeMirror instanceof DeclaredType declaredType)) {
            throw new IllegalArgumentException("Type mirror of type " + typeMirror.getClass().getName() + " cannot have type arguments");
        }

        if (isClass(typeMirror, i)) {
            return declaredType.getTypeArguments();
        }

        if(!(declaredType.asElement() instanceof TypeElement element)) {
            throw new IllegalArgumentException("Unexpected type element " + declaredType.asElement().getClass().getName() + "of declared type.");
        }

        var typeArguments = declaredType.getTypeArguments();
        var typeParameters = element.getTypeParameters();
        var aliases = new HashMap<String, TypeMirror>();

        for(var j = 0; j < typeParameters.size(); j++) {
            aliases.put(typeParameters.get(j).getSimpleName().toString(), typeArguments.get(j));
        }

        return getTypeArgumentsOfInterface(element, i, aliases);
    }

    private static List<TypeMirror> getTypeArgumentsOfInterface(TypeElement element, Class<?> i, Map<String, TypeMirror> aliases) {
        var optional = element.getInterfaces()
                .stream()
                .filter(mirror -> isClass(mapInterfaceMirrorToTypeElement(mirror), i))
                .findAny()
                .map(DeclaredType.class::cast);

        if (optional.isPresent()) {
            return optional.get()
                    .getTypeArguments()
                    .stream()
                    .map(e -> mapTypeArgument(aliases, e))
                    .toList();
        }

        var superClass = element.getSuperclass();
        if (superClass != null) {
            return getTypeArgumentsOfInterface(superClass, i).stream()
                    .map(e -> mapTypeArgument(aliases, e))
                    .toList();
        }

        throw new IllegalArgumentException("Type element " + element.getQualifiedName() + " does not implement interface " + i.getName());
    }

    private static TypeMirror mapTypeArgument(Map<String, TypeMirror> aliases, TypeMirror parameter) {
        if (parameter.getKind() != TypeKind.TYPEVAR) {
            return parameter;
        }

        var mappedParameter = aliases.get(parameter.toString());
        if (mappedParameter == null) {
            throw new IllegalArgumentException("Unmapped type parameter " + parameter);
        }

        return mappedParameter;
    }

    public static boolean isExtensionOf(TypeElement element, Class<?> c) {
        if (element.getKind() == ElementKind.INTERFACE) {
            return isImplementationOf(element, c);
        }

        if (isClass(element, c)) {
            return true;
        }

        if (element.getSuperclass() instanceof DeclaredType superClass) {
            return isExtensionOf((TypeElement) superClass.asElement(), c);
        } else {
            return false;
        }
    }

    public static boolean isClass(TypeMirror typeMirror, Class<?> c) {
        return typeMirror instanceof DeclaredType declaredType
                && declaredType.asElement() instanceof TypeElement element
                && isClass(element, c);
    }

    public static boolean isClass(TypeElement element, Class<?> c) {
        return element.getQualifiedName().toString().equals(c.getName());
    }

    public static List<VariableElement> getFields(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(field -> field instanceof VariableElement)
                .filter(field -> !(field instanceof RecordComponentElement))
                .map(VariableElement.class::cast)
                .toList();
    }

    public static boolean isAnnotationPresent(Element element, Class<? extends Annotation> annotation) {
        return element.getAnnotationMirrors()
                .stream()
                .map(AnnotationMirror::getAnnotationType)
                .map(TypeElementUtils::mapToTypeElement)
                .map(TypeElement::getQualifiedName)
                .map(Name::toString)
                .anyMatch(annotation.getName()::equals);
    }

    private static TypeElement mapInterfaceMirrorToTypeElement(TypeMirror mirror) {
        if (!(mirror instanceof DeclaredType)) {
            throw new IllegalArgumentException("Invalid interface type mirror " + mirror);
        }

        return mapToTypeElement((DeclaredType) mirror);
    }

    private static TypeElement mapToTypeElement(DeclaredType mirror) {
        var element = mirror.asElement();
        if (!(element instanceof TypeElement)) {
            throw new IllegalArgumentException("Unexpected return type of DeclaredType#asElement from for declared type " + mirror);
        }
        return (TypeElement) element;
    }
}
