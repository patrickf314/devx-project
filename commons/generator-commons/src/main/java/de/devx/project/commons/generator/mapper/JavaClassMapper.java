package de.devx.project.commons.generator.mapper;

import de.devx.project.commons.generator.model.*;
import de.devx.project.commons.generator.type.JavaAccessModifierType;
import de.devx.project.commons.generator.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public final class JavaClassMapper {

    private JavaClassMapper() {
        // No instances
    }

    public static JavaClassModel mapToClassModel(Class<?> c) {
        return new JavaClassModel(
                c.getPackageName(),
                ClassUtils.extractSimpleClassName(c.getName()),
                Arrays.stream(c.getTypeParameters())
                        .map(JavaClassMapper::mapToTypeArgumentModel)
                        .toList(),
                mapToTypeModel(c.getGenericSuperclass()),
                Arrays.stream(c.getDeclaredFields())
                        .map(JavaClassMapper::mapToFieldModel)
                        .toList(),
                Arrays.stream(c.getDeclaredMethods())
                        .map(JavaClassMapper::mapToMethodModel)
                        .toList(),
                Arrays.stream(c.getAnnotations())
                        .map(JavaClassMapper::mapToAnnotationModel)
                        .toList(),
                c.isRecord()
        );
    }

    private static JavaClassFieldModel mapToFieldModel(Field field) {
        return new JavaClassFieldModel(
                field.getName(),
                mapToTypeModel(field.getGenericType()),
                mapAccessModifiers(field.getModifiers())
        );
    }

    private static JavaClassMethodModel mapToMethodModel(Method method) {
        return new JavaClassMethodModel(
                method.getName(),
                mapAccessModifiers(method.getModifiers()),
                Arrays.stream(method.getTypeParameters())
                        .map(JavaClassMapper::mapToTypeArgumentModel)
                        .toList(),
                mapToTypeModel(method.getGenericReturnType()),
                Arrays.stream(method.getParameters())
                        .map(JavaClassMapper::mapToMethodParameterModel)
                        .toList()
        );
    }

    private static JavaAnnotationModel mapToAnnotationModel(Annotation annotation) {
        return new JavaAnnotationModel(
            mapToTypeModel(annotation.annotationType())
        );
    }

    private static JavaClassMethodParameterModel mapToMethodParameterModel(Parameter parameter) {
        return new JavaClassMethodParameterModel(
                parameter.getName(),
                mapToTypeModel(parameter.getParameterizedType())
        );
    }

    private static JavaTypeArgumentModel mapToTypeArgumentModel(TypeVariable<?> c) {
        var bounds = c.getBounds();
        if (bounds.length != 1) {
            throw new IllegalArgumentException("Mapping of no or more than one bounds of type arguments is not supported");
        }

        var bound = bounds[0];
        if (bound.getTypeName().equals("java.lang.Object")) {
            return new JavaTypeArgumentModel(c.getName());
        } else {
            return new JavaTypeArgumentModel(c.getName(), mapToTypeModel(bound));
        }
    }

    private static JavaTypeModel mapToTypeModel(Type type) {
        if (type instanceof TypeVariable<?> typeVariable) {
            return JavaTypeModel.genericTemplateType(typeVariable.getName());
        }

        if (type instanceof ParameterizedType parameterizedType) {
            var typeArguments = Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(JavaClassMapper::mapToTypeModel)
                    .toList();

            var rawType = mapToTypeModel(parameterizedType.getRawType());
            return JavaTypeModel.objectType(
                    rawType.getPackageName().orElseThrow(),
                    rawType.getName(),
                    typeArguments
            );
        }

        if (type instanceof Class<?> classType) {
            if (classType.isArray()) {
                return JavaTypeModel.arrayType(mapToTypeModel(classType.getComponentType()));
            }

            if (classType.isPrimitive()) {
                var name = classType.getSimpleName();
                var boxedType = ClassUtils.toBoxedType(name);
                return JavaTypeModel.primitiveType(name, boxedType.getSimpleName());
            }

            return JavaTypeModel.objectType(classType.getPackageName(), ClassUtils.extractSimpleClassName(classType.getName()));
        }

        throw new IllegalArgumentException("Mapping of type " + type + " is not supported");
    }

    private static Set<JavaAccessModifierType> mapAccessModifiers(int modifiers) {
        var accessModifiers = EnumSet.noneOf(JavaAccessModifierType.class);
        if (Modifier.isPublic(modifiers)) {
            accessModifiers.add(JavaAccessModifierType.PUBLIC);
        }
        if (Modifier.isProtected(modifiers)) {
            accessModifiers.add(JavaAccessModifierType.PROTECTED);
        }
        if (Modifier.isPrivate(modifiers)) {
            accessModifiers.add(JavaAccessModifierType.PRIVATE);
        }
        if (Modifier.isStatic(modifiers)) {
            accessModifiers.add(JavaAccessModifierType.STATIC);
        }
        if (Modifier.isFinal(modifiers)) {
            accessModifiers.add(JavaAccessModifierType.FINAL);
        }
        return accessModifiers;
    }
}
