package api.maven.plugin.processor.entity;

import api.maven.plugin.common.processor.utils.TypeElementUtils;
import api.maven.plugin.processor.entity.data.EntityField;
import api.maven.plugin.processor.entity.data.EntityFieldType;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static api.maven.plugin.processor.entity.data.EntityFieldType.*;

public class EntityMatcherGenerator {

    private final Messager messager;
    private final Filer filer;
    private final Configuration configuration;

    public EntityMatcherGenerator(Messager messager, Filer filer) {
        this.messager = messager;
        this.filer = filer;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(EntityMatcherGenerator.class.getClassLoader(), "templates");
    }

    public void generateMatchers(Set<? extends Element> annotatedElements) {
        for (var annotatedElement : annotatedElements) {
            try {
                generateMatcher(annotatedElement);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate entity matcher: " + e.getMessage(), annotatedElement);
            }
        }
    }

    private void generateMatcher(Element element) throws IOException {
        if (element.getKind() == ElementKind.CLASS) {
            generateMatcherForClass((TypeElement) element);
            return;
        }

        messager.printMessage(Diagnostic.Kind.ERROR, "Element of kind " + element.getKind() + " is not supported", element);
    }

    private void generateMatcherForClass(TypeElement element) throws IOException {
        var packageName = TypeElementUtils.getPackageName(element);
        var generics = element.getTypeParameters().stream().map(Object::toString).toList();
        var fields = getFields(element);
        var imports = streamImports(fields, packageName)
                .distinct()
                .sorted()
                .toList();

        if (fields.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Entity does not define any getters, no matcher will be created.", element);
            return;
        }

        var file = filer.createSourceFile(element.getQualifiedName() + "Matcher");
        try (var writer = file.openWriter()) {
            var template = configuration.getTemplate("EntityMatcher.java.ftl");

            var attributes = Map.of(
                    "packageName", packageName,
                    "entityName", element.getSimpleName().toString(),
                    "imports", imports,
                    "generics", generics.isEmpty() ? "" : "<" + String.join(", ", generics) + ">",
                    "fields", fields,
                    "matcherFactoryFunctionName", lowerFirstChar(element.getSimpleName().toString())
            );

            template.process(attributes, writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private List<EntityField> getFields(TypeElement element) {
        return element.getEnclosedElements()
                .stream()
                .filter(method -> method.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .filter(method -> !TypeElementUtils.isStatic(method))
                .filter(method -> method.getParameters().isEmpty())
                .filter(method -> method.getReturnType().getKind() != TypeKind.VOID)
                .filter(this::isGetter)
                .map(this::mapField)
                .toList();
    }

    private boolean isGetter(ExecutableElement element) {
        var getter = element.getSimpleName().toString();
        return getter.startsWith("get") || getter.startsWith("is");
    }

    private EntityField mapField(ExecutableElement element) {
        var getter = element.getSimpleName().toString();
        var name = lowerFirstChar(getter.substring(getter.startsWith("is") ? 2 : 3));
        return new EntityField(mapType(element.getReturnType()), name, getter);
    }

    private EntityFieldType mapType(TypeMirror type) {
        return switch (type.getKind()) {
            case INT, DOUBLE, FLOAT, BYTE, LONG, SHORT, BOOLEAN, VOID -> mapPrimaryType(type);
            case ARRAY -> arrayType(mapType(((ArrayType) type).getComponentType()));
            case DECLARED -> mapDeclaredType((DeclaredType) type);
            case TYPEVAR -> genericType(type.toString());
            default ->
                    throw new IllegalArgumentException("Unexpected type mirror " + type + "(" + type.getClass().getName() + ")");
        };
    }

    private static EntityFieldType mapPrimaryType(TypeMirror type) {
        var className = type.getKind() == TypeKind.INT ? "Integer" : type.getKind().name().charAt(0) + type.getKind().name().substring(1).toLowerCase(Locale.ROOT);
        return primaryType(type.getKind().name().toLowerCase(Locale.ROOT), className);
    }

    private EntityFieldType mapDeclaredType(DeclaredType type) {
        if (!(type.asElement() instanceof TypeElement element)) {
            throw new IllegalArgumentException("Unexpected type mirror " + type.getClass().getName() + ", asElement() does not return a TypeElement");
        }

        var generics = type.getTypeArguments()
                .stream()
                .map(this::mapType)
                .toList();

        return objectType(TypeElementUtils.getPackageName(element), element.getSimpleName().toString(), generics);
    }

    private String lowerFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    private Stream<String> streamImports(List<EntityField> fields, String packageName) {
        var checkedClasses = new HashSet<String>();
        return fields.stream()
                .map(EntityField::getType)
                .flatMap(type -> streamImports(type, packageName, checkedClasses));
    }

    private Stream<String> streamImports(EntityFieldType type, String packageName, Set<String> checkedClasses) {
        if (type.getPackageName() == null || "java.lang".equals(type.getPackageName()) || packageName.equals(type.getPackageName())) {
            return Stream.empty();
        }

        var fullName = type.getPackageName() + "." + type.getClassName();

        if (checkedClasses.contains(fullName)) {
            return Stream.empty();
        }
        checkedClasses.add(fullName);

        var generics = type.getGenerics()
                .stream()
                .flatMap(genericType -> streamImports(genericType, packageName, checkedClasses));

        return Stream.concat(Stream.of(fullName), generics);
    }
}
