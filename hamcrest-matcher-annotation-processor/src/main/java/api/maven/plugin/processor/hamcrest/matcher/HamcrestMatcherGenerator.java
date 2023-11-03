package api.maven.plugin.processor.hamcrest.matcher;

import api.maven.plugin.common.processor.utils.TypeElementUtils;
import api.maven.plugin.processor.hamcrest.matcher.data.ClassField;
import api.maven.plugin.processor.hamcrest.matcher.data.ClassFieldType;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static api.maven.plugin.processor.hamcrest.matcher.data.ClassFieldType.*;

public class HamcrestMatcherGenerator {

    private final Messager messager;
    private final Filer filer;
    private final Configuration configuration;

    public HamcrestMatcherGenerator(Messager messager, Filer filer) {
        this.messager = messager;
        this.filer = filer;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(HamcrestMatcherGenerator.class.getClassLoader(), "templates");
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

        if (element.getKind() == ElementKind.RECORD) {
            generateMatcherForRecord((TypeElement) element);
            return;
        }

        messager.printMessage(Diagnostic.Kind.ERROR, "Element of kind " + element.getKind() + " is not supported", element);
    }

    private void generateMatcherForRecord(TypeElement element) throws IOException {
        var fields = element.getEnclosedElements()
                .stream()
                .filter(field -> field.getKind() == ElementKind.RECORD_COMPONENT)
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .map(field -> new ClassField(mapType(field.asType()), field.getSimpleName().toString(), field.getSimpleName().toString()))
                .toList();

        generateMatcher(element, fields);
    }

    private void generateMatcherForClass(TypeElement element) throws IOException {
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

        generateMatcher(element, fields);
    }

    private void generateMatcher(TypeElement element, List<ClassField> fields) throws IOException {
        var generics = element.getTypeParameters().stream().map(Object::toString).toList();
        var packageName = TypeElementUtils.getPackageName(element);
        var imports = streamImports(fields, packageName)
                .distinct()
                .sorted()
                .toList();
        var className = getClassName(element);
        var classType = getClassType(element);

        if (fields.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Entity does not define any getters, no matcher will be created.", element);
            return;
        }

        var file = filer.createSourceFile(packageName + "." + className + "Matcher");
        try (var writer = file.openWriter()) {
            var template = configuration.getTemplate("HamcrestMatcher.ftl");

            var attributes = Map.of(
                    "packageName", packageName,
                    "entityName", className,
                    "entityType", classType,
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

    private static String getClassName(TypeElement element) {
        if(element.getEnclosingElement() instanceof TypeElement enclosingTypeElement) {
            return getClassName(enclosingTypeElement) + element.getSimpleName();
        }

        return element.getSimpleName().toString();
    }

    private static String getClassType(TypeElement element) {
        if(element.getEnclosingElement() instanceof TypeElement enclosingTypeElement) {
            return getClassName(enclosingTypeElement) + "." + element.getSimpleName();
        }

        return element.getSimpleName().toString();
    }

    private boolean isGetter(ExecutableElement element) {
        var getter = element.getSimpleName().toString();
        return getter.startsWith("get") || getter.startsWith("is");
    }

    private ClassField mapField(ExecutableElement element) {
        var getter = element.getSimpleName().toString();
        var name = lowerFirstChar(getter.substring(getter.startsWith("is") ? 2 : 3));
        return new ClassField(mapType(element.getReturnType()), name, getter);
    }

    private ClassFieldType mapType(TypeMirror type) {
        return switch (type.getKind()) {
            case INT, DOUBLE, FLOAT, BYTE, LONG, SHORT, BOOLEAN, VOID -> mapPrimaryType(type);
            case ARRAY -> arrayType(mapType(((ArrayType) type).getComponentType()));
            case DECLARED -> mapDeclaredType((DeclaredType) type);
            case TYPEVAR -> genericType(type.toString());
            default ->
                    throw new IllegalArgumentException("Unexpected type mirror " + type + "(" + type.getClass().getName() + ")");
        };
    }

    private static ClassFieldType mapPrimaryType(TypeMirror type) {
        var className = type.getKind() == TypeKind.INT ? "Integer" : type.getKind().name().charAt(0) + type.getKind().name().substring(1).toLowerCase(Locale.ROOT);
        return primaryType(type.getKind().name().toLowerCase(Locale.ROOT), className);
    }

    private ClassFieldType mapDeclaredType(DeclaredType type) {
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

    private Stream<String> streamImports(List<ClassField> fields, String packageName) {
        var checkedClasses = new HashSet<String>();
        return fields.stream()
                .map(ClassField::getType)
                .flatMap(type -> streamImports(type, packageName, checkedClasses));
    }

    private Stream<String> streamImports(ClassFieldType type, String packageName, Set<String> checkedClasses) {
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
