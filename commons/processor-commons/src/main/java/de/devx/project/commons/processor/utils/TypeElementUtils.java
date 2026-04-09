package de.devx.project.commons.processor.utils;

import de.devx.project.commons.processor.ProcessorContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.devx.project.commons.processor.utils.AnnotationElementUtils.isAnnotationPresent;
import static java.util.function.Predicate.not;

public final class TypeElementUtils {

    private TypeElementUtils() {
        // No instances
    }

    public static boolean isImplementationOf(TypeMirror typeMirror, Class<?> i) {
        return isImplementationOf(typeMirror, i.getName());
    }

    public static boolean isImplementationOf(TypeMirror typeMirror, String i) {
        return getInterfaceTypeMirror(typeMirror, i).isPresent();
    }

    public static boolean isImplementationOf(TypeElement typeElement, Class<?> i) {
        return isImplementationOf(typeElement, i.getName());
    }

    public static boolean isImplementationOf(TypeElement typeElement, String i) {
        return getInterfaceTypeMirror(typeElement, i).isPresent();
    }

    public static Optional<TypeMirror> getInterfaceTypeMirror(TypeMirror typeMirror, Class<?> i) {
        return getInterfaceTypeMirror(typeMirror, i.getName());
    }

    public static Optional<TypeMirror> getInterfaceTypeMirror(TypeMirror typeMirror, String i) {
        if (!(typeMirror instanceof DeclaredType declaredType) || !(declaredType.asElement() instanceof TypeElement element)) {
            return Optional.empty();
        }

        return getInterfaceTypeMirror(element, i);
    }

    public static Optional<TypeMirror> getInterfaceTypeMirror(TypeElement typeElement, Class<?> i) {
        return getInterfaceTypeMirror(typeElement, i.getName());
    }

    public static Optional<TypeMirror> getInterfaceTypeMirror(TypeElement typeElement, String i) {
        if (isClass(typeElement, i)) {
            return Optional.of(typeElement.asType());
        }

        var optional = typeElement.getInterfaces()
                .stream()
                .map(mirror -> {
                    if(isClass(mirror, i)) {
                        return Optional.<TypeMirror>of(mirror);
                    }else{
                        return getInterfaceTypeMirror(mirror, i);
                    }
                })
                .flatMap(Optional::stream)
                .findAny();

        if (optional.isPresent()) {
            return optional;
        }

        var superClass = typeElement.getSuperclass();
        if (!(superClass instanceof NoType)) {
            return getInterfaceTypeMirror(superClass, i);
        }

        return Optional.empty();
    }

    public static List<TypeMirror> getTypeArgumentsOfInterface(TypeMirror typeMirror, Class<?> i) {
        if (!(typeMirror instanceof DeclaredType declaredType)) {
            throw new IllegalArgumentException("Type mirror of type " + typeMirror.getClass().getName() + " cannot have type arguments");
        }

        if (isClass(typeMirror, i)) {
            return new ArrayList<>(declaredType.getTypeArguments());
        }

        if (!(declaredType.asElement() instanceof TypeElement element)) {
            throw new IllegalArgumentException("Unexpected type element " + declaredType.asElement().getClass().getName() + "of declared type.");
        }

        var typeArguments = declaredType.getTypeArguments();
        var typeParameters = element.getTypeParameters();
        var aliases = new HashMap<String, TypeMirror>();

        for (var j = 0; j < typeParameters.size(); j++) {
            aliases.put(typeParameters.get(j).getSimpleName().toString(), typeArguments.get(j));
        }

        return getTypeArgumentsOfInterface(element, i, aliases);
    }

    private static List<TypeMirror> getTypeArgumentsOfInterface(TypeElement element, Class<?> i, Map<String, TypeMirror> aliases) {
        var optional = element.getInterfaces()
                .stream()
                .filter(mirror -> isImplementationOf(mirror, i))
                .findAny()
                .map(DeclaredType.class::cast);

        if (optional.isPresent()) {
            return getTypeArgumentsOfInterface(optional.get(), i)
                    .stream()
                    .map(e -> mapTypeArgument(aliases, e))
                    .toList();
        }

        var superClass = element.getSuperclass();
        if (!(superClass instanceof NoType)) {
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
        return isClass(typeMirror, c.getName());
    }

    public static boolean isClass(TypeMirror typeMirror, String c) {
        return typeMirror instanceof DeclaredType declaredType
               && declaredType.asElement() instanceof TypeElement element
               && isClass(element, c);
    }

    public static boolean isClass(TypeElement element, Class<?> c) {
        return isClass(element, c.getName());
    }

    public static boolean isClass(TypeElement element, String c) {
        return c.equals(element.getQualifiedName().toString());
    }

    public static boolean isBrandedType(TypeElement element) {
        return isAnnotationPresent(element, "de.devx.project.annotations.BrandedType");
    }

    /**
     * Returns the explicit underlying type declared in {@code @BrandedType(type = ...)} for the
     * given element, or {@link Optional#empty()} if no explicit type was set (i.e. the attribute
     * is still the default {@code void.class}) or the element is not a branded type.
     */
    public static Optional<TypeMirror> getBrandedTypeExplicitTypeMirror(TypeElement element) {
        return AnnotationElementUtils.findAnnotationMirror(element, "de.devx.project.annotations.BrandedType")
                .flatMap(mirror -> {
                    var fields = AnnotationElementUtils.extractFieldsFromAnnotationMirror(mirror);
                    var typeValue = fields.get("type");
                    if (typeValue == null) {
                        return Optional.empty();
                    }
                    var typeMirror = (TypeMirror) typeValue.getValue();
                    if (typeMirror.getKind() == TypeKind.VOID) {
                        return Optional.empty();
                    }
                    return Optional.of(typeMirror);
                });
    }


    public static List<VariableElement> getNonStaticFields(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(not(TypeElementUtils::isStatic))
                .filter(VariableElement.class::isInstance)
                .filter(not(RecordComponentElement.class::isInstance))
                .map(VariableElement.class::cast)
                .toList();
    }

    public static List<VariableElement> getEnumValues(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(TypeElementUtils::isStatic)
                .filter(VariableElement.class::isInstance)
                .filter(not(RecordComponentElement.class::isInstance))
                .map(VariableElement.class::cast)
                .toList();
    }

    public static boolean isStatic(Element element) {
        return element.getModifiers().contains(Modifier.STATIC);
    }

    public static String getPackageName(TypeElement typeElement) {
        var enclosingElement = typeElement.getEnclosingElement();
        if (enclosingElement instanceof PackageElement packageElement) {
            return packageElement.getQualifiedName().toString();
        }

        if (enclosingElement instanceof TypeElement enclosingTypeElement) {
            return getPackageName(enclosingTypeElement);
        }

        throw new IllegalArgumentException("Failed to get package name for type element " + typeElement.getQualifiedName());
    }

    public static boolean checkTypeMirrorsEquality(List<? extends TypeMirror> typeMirrorsA, List<? extends TypeMirror> typeMirrorsB) {
        if(typeMirrorsA.size() != typeMirrorsB.size()) {
            return false;
        }

        for (int i = 0; i < typeMirrorsA.size(); i++) {
            if(!checkTypeMirrorEquality(typeMirrorsA.get(i), typeMirrorsB.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkTypeMirrorEquality(TypeMirror typeMirrorA, TypeMirror typeMirrorB) {
        return ProcessorContext.getProcessingEnvironment().getTypeUtils().isSameType(typeMirrorA, typeMirrorB);
    }
}
