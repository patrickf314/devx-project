package de.devx.project.commons.maven.mapper;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.resolution.declarations.ResolvedAnnotationDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import de.devx.project.commons.generator.logging.Logger;
import de.devx.project.commons.generator.model.JavaAnnotationModel;
import de.devx.project.commons.generator.model.JavaClassFieldModel;
import de.devx.project.commons.generator.model.JavaClassMethodModel;
import de.devx.project.commons.generator.model.JavaClassMethodParameterModel;
import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import de.devx.project.commons.generator.type.JavaAccessModifierType;
import de.devx.project.commons.generator.utils.ClassUtils;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class MavenClassMapper {

    private final Logger log;

    public JavaClassModel mapToClassModel(ResolvedReferenceTypeDeclaration type) {
        try {
            return new JavaClassModel(
                    extractPackageName(type),
                    extractClassName(type),
                    type.getTypeParameters().stream()
                            .map(this::mapToTypeArgumentModel)
                            .toList(),
                    type.getAncestors()
                            .stream()
                            .filter(ancestor -> ancestor.getTypeDeclaration().filter(ResolvedTypeDeclaration::isClass).isPresent())
                            .findFirst()
                            .map(this::mapToTypeModel)
                            .orElse(null),
                    type.getDeclaredFields().stream()
                            .map(this::mapToFieldModel)
                            .toList(),
                    type.getDeclaredMethods().stream()
                            .map(this::mapToMethodModel)
                            .toList(),
                    type.getDeclaredAnnotations().stream()
                            .map(this::mapToAnnotation)
                            .toList(),
                    type.isRecord()
            );
        } catch (RuntimeException e) {
            log.info("Failed to resolve " + type.getQualifiedName() + ", this class will be ignored: " + e.getMessage());
            return null;
        }
    }

    private JavaClassFieldModel mapToFieldModel(ResolvedFieldDeclaration field) {
        return new JavaClassFieldModel(
                field.getName(),
                mapToTypeModel(field.getType()),
                mapToAccessModifiers(field.accessSpecifier(), field.isStatic())
        );
    }

    private JavaClassMethodModel mapToMethodModel(ResolvedMethodDeclaration method) {
        return new JavaClassMethodModel(
                method.getName(),
                mapToAccessModifiers(method.accessSpecifier(), method.isStatic()),
                method.getTypeParameters().stream()
                        .map(this::mapToTypeArgumentModel)
                        .toList(),
                mapToTypeModel(method.getReturnType()),
                IntStream.range(0, method.getNumberOfParams())
                        .mapToObj(method::getParam)
                        .map(this::mapToMethodParameterModel)
                        .toList()
        );
    }

    private JavaClassMethodParameterModel mapToMethodParameterModel(ResolvedParameterDeclaration parameter) {
        return new JavaClassMethodParameterModel(
                parameter.getName(),
                mapToTypeModel(parameter.getType())
        );
    }

    private JavaTypeArgumentModel mapToTypeArgumentModel(ResolvedTypeParameterDeclaration type) {
        return new JavaTypeArgumentModel(
                type.getName(),
                type.hasUpperBound() ? mapToTypeModel(type.getUpperBound()) : null
        );
    }

    private Set<JavaAccessModifierType> mapToAccessModifiers(AccessSpecifier accessSpecifier, boolean isStatic) {
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

    private JavaAnnotationModel mapToAnnotation(ResolvedAnnotationDeclaration annotation) {
        return new JavaAnnotationModel(
                JavaTypeModel.objectType(annotation.getPackageName(), annotation.getName())
        );
    }

    private JavaTypeModel mapToTypeModel(ResolvedType type) {
        if (type.isPrimitive()) {
            var primitive = type.asPrimitive();
            return JavaTypeModel.primitiveType(primitive.name().toLowerCase(Locale.ROOT), ClassUtils.extractSimpleClassName(primitive.getBoxTypeClass()));
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
                            .map(this::mapToTypeModel)
                            .toList()
            );
        }

        if (type.isVoid()) {
            return JavaTypeModel.VOID;
        }

        if (type.isWildcard()) {
            return JavaTypeModel.wildcardType();
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getClass().getSimpleName());
    }

    private String extractPackageName(ResolvedReferenceType type) {
        return type.getTypeDeclaration()
                .map(this::extractPackageName)
                .orElseThrow();
    }

    private String extractPackageName(ResolvedReferenceTypeDeclaration typeDeclaration) {
        return typeDeclaration.getPackageName();
    }

    private String extractClassName(ResolvedReferenceType type) {
        var packageName = extractPackageName(type);
        return type.getQualifiedName().substring(packageName.length() + 1);
    }

    private String extractClassName(ResolvedReferenceTypeDeclaration typeDeclaration) {
        var packageName = extractPackageName(typeDeclaration);
        return typeDeclaration.getQualifiedName().substring(packageName.length() + 1);
    }
}
