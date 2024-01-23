package de.devx.project.hamcrest.matcher.processor.mapper;

import de.devx.project.commons.generator.logging.Logger;
import de.devx.project.commons.processor.utils.TypeElementUtils;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestMatcherModel;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel.*;

public class HamcrestMatcherElementMapper {

    private final Logger logger;

    public HamcrestMatcherElementMapper(Logger logger) {
        this.logger = logger;
    }

    public Optional<HamcrestMatcherModel> mapToMatcher(Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            return mapClassToMatcher((TypeElement) element);
        }

        if (element.getKind() == ElementKind.RECORD) {
            return mapRecordToMatcher((TypeElement) element);
        }

        logger.error("Element of kind " + element.getKind() + " is not supported", element);
        return Optional.empty();
    }

    private Optional<HamcrestMatcherModel> mapRecordToMatcher(TypeElement element) {
        var fields = element.getEnclosedElements()
                .stream()
                .filter(field -> field.getKind() == ElementKind.RECORD_COMPONENT)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .map(this::mapField)
                .toList();

        return mapToMatcher(element, fields);
    }

    private Optional<HamcrestMatcherModel> mapClassToMatcher(TypeElement element) {
        var fields = element.getEnclosedElements()
                .stream()
                .filter(method -> method.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .filter(method -> !TypeElementUtils.isStatic(method))
                .filter(method -> method.getParameters().isEmpty())
                .filter(method -> method.getReturnType().getKind() != TypeKind.VOID)
                .filter(method -> method.getModifiers().contains(Modifier.PUBLIC))
                .filter(this::isGetter)
                .map(this::mapField)
                .toList();

        return mapToMatcher(element, fields);
    }

    private Optional<HamcrestMatcherModel> mapToMatcher(TypeElement element, List<HamcrestClassFieldModel> fields) {
        if (fields.isEmpty()) {
            logger.warn("Entity does not define any getters, no matcher will be created.", element);
            return Optional.empty();
        }

        var generics = element.getTypeParameters().stream().map(Object::toString).toList();
        var packageName = TypeElementUtils.getPackageName(element);
        var className = getClassName(element);

        var model = new HamcrestMatcherModel();
        model.setPackageName(packageName);
        model.setClassName(className);
        if (!generics.isEmpty()) {
            model.setGenerics("<" + String.join(", ", generics) + ">");
        }
        model.setFields(fields);
        return Optional.of(model);
    }

    private static String getClassName(TypeElement element) {
        if (element.getEnclosingElement() instanceof TypeElement enclosingTypeElement) {
            return getClassName(enclosingTypeElement) + element.getSimpleName();
        }

        return element.getSimpleName().toString();
    }

    private boolean isGetter(ExecutableElement element) {
        var getter = element.getSimpleName().toString();
        return getter.startsWith("get") || getter.startsWith("is");
    }

    private HamcrestClassFieldModel mapField(ExecutableElement element) {
        var getter = element.getSimpleName().toString();
        var name = lowerFirstChar(getter.substring(getter.startsWith("is") ? 2 : 3));
        return new HamcrestClassFieldModel(mapType(element.getReturnType()), name, getter);
    }

    private HamcrestClassFieldModel mapField(VariableElement element) {
        var name = element.getSimpleName().toString();
        return new HamcrestClassFieldModel(mapType(element.asType()), name, name);
    }

    private HamcrestClassFieldTypeModel mapType(TypeMirror type) {
        return switch (type.getKind()) {
            case INT, DOUBLE, FLOAT, BYTE, LONG, SHORT, BOOLEAN, VOID -> mapPrimaryType(type);
            case ARRAY -> arrayType(mapType(((ArrayType) type).getComponentType()));
            case DECLARED -> mapDeclaredType((DeclaredType) type);
            case TYPEVAR -> genericType(type.toString());
            default ->
                    throw new IllegalArgumentException("Unexpected type mirror " + type + "(" + type.getClass().getName() + ")");
        };
    }

    private static HamcrestClassFieldTypeModel mapPrimaryType(TypeMirror type) {
        var className = type.getKind() == TypeKind.INT ? "Integer" : type.getKind().name().charAt(0) + type.getKind().name().substring(1).toLowerCase(Locale.ROOT);
        return primaryType(type.getKind().name().toLowerCase(Locale.ROOT), className);
    }

    private HamcrestClassFieldTypeModel mapDeclaredType(DeclaredType type) {
        if (!(type.asElement() instanceof TypeElement element)) {
            throw new IllegalArgumentException("Unexpected type mirror " + type.getClass().getName() + ", asElement() does not return a TypeElement");
        }

        var generics = type.getTypeArguments()
                .stream()
                .map(this::mapType)
                .toList();

        return objectType(TypeElementUtils.getPackageName(element), getClassName(element), generics);
    }

    private String lowerFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
