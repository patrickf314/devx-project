package de.devx.project.commons.maven.mapper;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import de.devx.project.commons.generator.model.*;
import de.devx.project.commons.generator.type.JavaAccessModifierType;
import de.devx.project.commons.generator.utils.ClassUtils;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

public final class MavenClassMapper {

    private MavenClassMapper() {
        // No instances
    }

    public static JavaClassModel mapToClassModel(ResolvedReferenceTypeDeclaration type) {
        return new JavaClassModel(
                extractPackageName(type),
                extractClassName(type),
                type.getTypeParameters().stream()
                        .map(MavenClassMapper::mapToTypeArgumentModel)
                        .toList(),
                type.getAncestors()
                        .stream()
                        .filter(ancestor -> ancestor.getTypeDeclaration().filter(ResolvedTypeDeclaration::isClass).isPresent())
                        .findFirst()
                        .map(MavenClassMapper::mapToTypeModel)
                        .orElse(null),
                type.getDeclaredFields().stream()
                        .map(MavenClassMapper::mapToFieldModel)
                        .toList(),
                type.getDeclaredMethods().stream()
                        .map(MavenClassMapper::mapToMethodModel)
                        .toList(),
                type.getDeclaredAnnotations().stream()
                        .map(MavenClassMapper::mapToAnnotation)
                        .toList(),
                type.isRecord()
        );
    }

    private static JavaClassFieldModel mapToFieldModel(ResolvedFieldDeclaration field) {
        return new JavaClassFieldModel(
                field.getName(),
                mapToTypeModel(field.getType()),
                mapToAccessModifiers(field.accessSpecifier(), field.isStatic())
        );
    }

    private static JavaClassMethodModel mapToMethodModel(ResolvedMethodDeclaration method) {
        return new JavaClassMethodModel(
                method.getName(),
                mapToAccessModifiers(method.accessSpecifier(), method.isStatic()),
                method.getTypeParameters().stream()
                        .map(MavenClassMapper::mapToTypeArgumentModel)
                        .toList(),
                mapToTypeModel(method.getReturnType()),
                IntStream.range(0, method.getNumberOfParams())
                        .mapToObj(method::getParam)
                        .map(MavenClassMapper::mapToMethodParameterModel)
                        .toList()
        );
    }

    private static JavaClassMethodParameterModel mapToMethodParameterModel(ResolvedParameterDeclaration parameter) {
        return new JavaClassMethodParameterModel(
                parameter.getName(),
                mapToTypeModel(parameter.getType())
        );
    }

    private static JavaTypeArgumentModel mapToTypeArgumentModel(ResolvedTypeParameterDeclaration type) {
        return new JavaTypeArgumentModel(
                type.getName(),
                type.hasUpperBound() ? mapToTypeModel(type.getUpperBound()) : null
        );
    }

    private static Set<JavaAccessModifierType> mapToAccessModifiers(AccessSpecifier accessSpecifier, boolean isStatic) {
        var modifiers = EnumSet.noneOf(JavaAccessModifierType.class);
        if (isStatic) {
            modifiers.add(JavaAccessModifierType.STATIC);
        }

        switch (accessSpecifier) {
            case PUBLIC -> modifiers.add(JavaAccessModifierType.PUBLIC);
            case PROTECTED -> modifiers.add(JavaAccessModifierType.PROTECTED);
            case PRIVATE -> modifiers.add(JavaAccessModifierType.PRIVATE);
            default -> { /* do nothing */ }
        }

        return modifiers;
    }

    private static JavaAnnotationModel mapToAnnotation(ResolvedAnnotationDeclaration annotation) {
        return new JavaAnnotationModel(
                JavaTypeModel.objectType(annotation.getPackageName(), annotation.getName())
        );
    }

    private static JavaTypeModel mapToTypeModel(ResolvedType type) {
        if (type.isPrimitive()) {
            var primitive = type.asPrimitive();
            return JavaTypeModel.primitiveType(primitive.name().toLowerCase(Locale.ROOT), ClassUtils.extractSimpleClassName(primitive.getBoxTypeQName()));
        }

        if (type.isArray()) {
            var array = type.asArrayType();
            return JavaTypeModel.arrayType(mapToTypeModel(array.getComponentType()));
        }

        if (type.isTypeVariable()) {
            var typeVariable = type.asTypeParameter();
            return JavaTypeModel.genericTemplateType(typeVariable.getName());
        }

        if (type.isReferenceType()) {
            var classOrInterface = type.asReferenceType();
            return JavaTypeModel.objectType(
                    extractPackageName(classOrInterface),
                    extractClassName(classOrInterface),
                    classOrInterface.getTypeParametersMap()
                            .stream()
                            .map(p -> p.b)
                            .map(MavenClassMapper::mapToTypeModel)
                            .toList()
            );
        }

        if (type.isVoid()) {
            return JavaTypeModel.VOID;
        }

        if (type.isWildcard()) {
            return JavaTypeModel.WILDCARD;
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getClass().getSimpleName());
    }

    private static String extractPackageName(ResolvedReferenceType type) {
        return type.getTypeDeclaration()
                .map(MavenClassMapper::extractPackageName)
                .orElse(ClassUtils.extractPackageName(type.getQualifiedName()));
    }

    private static String extractPackageName(ResolvedReferenceTypeDeclaration typeDeclaration) {
        return typeDeclaration.getPackageName();
    }

    private static String extractClassName(ResolvedReferenceType type) {
        return type.getTypeDeclaration()
                .map(MavenClassMapper::extractClassName)
                .orElse(ClassUtils.extractSimpleClassName(type.getQualifiedName()));
    }

    private static String extractClassName(ResolvedReferenceTypeDeclaration typeDeclaration) {
        var qualifiedName = typeDeclaration.getQualifiedName();
        var packageName = typeDeclaration.getPackageName();

        return qualifiedName.substring(packageName.length() + 1).replace('.', '$');
    }
}
