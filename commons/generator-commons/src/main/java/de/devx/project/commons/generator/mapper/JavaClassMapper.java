package de.devx.project.commons.generator.mapper;

import de.devx.project.commons.generator.model.*;
import de.devx.project.commons.generator.type.JavaAccessModifierType;
import de.devx.project.commons.generator.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public final class JavaClassMapper {

    private JavaClassMapper() {
        // No instances
    }

    public static JavaClassModel mapToClassModel(Class<?> c) {
        return new JavaClassModel(
                c.getPackageName(),
                extractSimpleClassName(c),
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
            return new JavaTypeArgumentModel(c.getTypeName());
        } else {
            return new JavaTypeArgumentModel(c.getTypeName(), mapToTypeModel(bound));
        }
    }

    private static JavaTypeModel mapToTypeModel(Type type) {
        return mapToTypeModel(type, new HashMap<>());
    }

    private static JavaTypeModel mapToTypeModel(Type type, Map<Type, JavaTypeModel> typeContext) {
        if (type == null) {
            return null;
        }

        var modelFromContext = typeContext.get(type);
        if (modelFromContext != null) {
            return modelFromContext;
        }

        if (type instanceof TypeVariable<?> typeVariable) {
            var model = JavaTypeModel.genericTemplateType(typeVariable.getTypeName());
            typeContext.put(type, model);

            var bounds = typeVariable.getBounds();
            if (bounds.length > 1) {
                model.setTypeConstraint(mapToTypeModel(bounds[0], typeContext));
            }

            return model;
        }

        if (type instanceof ParameterizedType parameterizedType) {
            var rawType = mapToTypeModel(parameterizedType.getRawType(), typeContext);
            var model = JavaTypeModel.objectType(
                    rawType.getPackageName().orElseThrow(),
                    rawType.getName());
            typeContext.put(type, model);

            model.setTypeArguments(Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(typeArgument -> JavaClassMapper.mapToTypeModel(typeArgument, typeContext))
                    .toList());

            return model;
        }

        if (type instanceof Class<?> classType) {
            if (classType.isArray()) {
                return JavaTypeModel.arrayType(mapToTypeModel(classType.getComponentType(), typeContext));
            }

            if (classType.isPrimitive()) {
                var name = classType.getSimpleName();
                var boxedType = ClassUtils.toBoxedType(name);
                return JavaTypeModel.primitiveType(name, boxedType.getSimpleName());
            }

            var packageName = classType.getPackageName();
            var className = extractSimpleClassName(classType);

            return JavaTypeModel.objectType(packageName, className);
        }

        if (type instanceof WildcardType wildcardType) {
            var model = JavaTypeModel.wildcardType();

            var bounds = wildcardType.getUpperBounds();
            if (bounds.length > 1) {
                model.setTypeConstraint(mapToTypeModel(bounds[0], typeContext));
            }

            return model;
        }

        if (type instanceof GenericArrayType genericArrayType) {
            return JavaTypeModel.arrayType(mapToTypeModel(genericArrayType.getGenericComponentType(), typeContext));
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

    private static String extractSimpleClassName(Class<?> c) {
        var simpleName = c.getSimpleName();
        if (c.getEnclosingClass() != null) {
            return extractSimpleClassName(c.getEnclosingClass()) + "." + simpleName;
        }
        return simpleName;
    }
}
