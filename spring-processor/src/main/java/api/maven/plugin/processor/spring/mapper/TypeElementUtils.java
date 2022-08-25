package api.maven.plugin.processor.spring.mapper;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TypeElementUtils {

    private TypeElementUtils() {
        // No instances
    }

    public static boolean isImplementationOf(TypeElement element, Class<?> i) {
        if (isClass(element, i)) {
            return true;
        }

        return getAllInterfacesOf(element).contains(i.getName());
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

    public static boolean isClass(TypeElement element, Class<?> i) {
        return element.getQualifiedName().toString().equals(i.getName());
    }

    public static List<VariableElement> getFields(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(field -> field instanceof VariableElement)
                .filter(field -> !(field instanceof RecordComponentElement))
                .map(VariableElement.class::cast)
                .toList();
    }

    public static Set<String> getAllInterfacesOf(TypeElement element) {
        var set = new HashSet<String>();
        addAllInterfacesOf(element, set);

        var superClass = element.getSuperclass();
        if (superClass instanceof DeclaredType) {
            addAllInterfacesOf(((DeclaredType) superClass).asElement(), set);
        }

        return set;
    }

    private static void addAllInterfacesOf(Element element, Set<String> set) {
        if (!(element instanceof TypeElement)) {
            throw new IllegalArgumentException("Can only extract interfaces from type element, but element was " + element.getClass().getName());
        }

        var interfaces = ((TypeElement) element).getInterfaces()
                .stream()
                .map(TypeElementUtils::getInterfaceElement).toList();

        interfaces.forEach(e -> addAllInterfacesOf(e, set));

        set.addAll(interfaces.stream().map(e -> e.getQualifiedName().toString()).toList());
    }

    private static TypeElement getInterfaceElement(TypeMirror mirror) {
        if (!(mirror instanceof DeclaredType)) {
            throw new IllegalArgumentException("Invalid interface type mirror " + mirror);
        }

        return mapToTypeElement((DeclaredType) mirror);
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

    private static TypeElement mapToTypeElement(DeclaredType mirror) {
        var element = mirror.asElement();
        if (!(element instanceof TypeElement)) {
            throw new IllegalArgumentException("Unexpected return type of DeclaredType#asElement from for declared type " + mirror);
        }
        return (TypeElement) element;
    }
}
