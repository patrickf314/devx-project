package api.maven.plugin.dto;

import api.maven.plugin.core.model.ApiDTOModel;
import api.maven.plugin.core.model.ApiModel;
import api.maven.plugin.core.model.ApiTypeModel;
import api.maven.plugin.core.type.ApiTypeType;
import api.maven.plugin.dto.data.DTOField;
import api.maven.plugin.dto.data.DTOFieldType;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static api.maven.plugin.dto.data.DTOFieldType.*;

public class DTOMatcherGenerator {

    private static final String JAVA_LANG_PACKAGE = "java.lang";

    private final String outputDirectory;
    private final Configuration configuration;

    public DTOMatcherGenerator(String outputDirectory) {
        this.outputDirectory = outputDirectory;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(DTOMatcherGenerator.class.getClassLoader(), "templates");
    }

    public void generateMatchers(ApiModel apiModel) throws IOException {
        for (var dtoModel : apiModel.getDtos().values()) {
            if (dtoModel.getClassName().startsWith("java.")) {
                continue;
            }

            generateMatcher(dtoModel, apiModel);
        }
    }

    private void generateMatcher(ApiDTOModel dtoModel, ApiModel apiModel) throws IOException {
        var packageName = packageName(dtoModel.getClassName());
        var generics = dtoModel.getTypeArguments();
        var fields = getFields(dtoModel, apiModel);
        var imports = streamImports(fields, packageName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (fields.isEmpty()) {
            return;
        }

        var file = new File(createPackageDirectory(packageName), dtoModel.getName() + "Matcher.java");
        try (var writer = new FileWriter(file)) {
            var template = configuration.getTemplate("DTOMatcher.java.ftl");

            var attributes = Map.of(
                    "packageName", packageName,
                    "dtoName", dtoModel.getName(),
                    "imports", imports,
                    "generics", generics.isEmpty() ? "" : "<" + String.join(", ", generics) + ">",
                    "fields", fields,
                    "matcherFactoryFunctionName", lowerFirstChar(dtoModel.getName())
            );

            template.process(attributes, writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private List<DTOField> getFields(ApiDTOModel dtoModel, ApiModel apiModel) {
        var list = new ArrayList<DTOField>();
        if (dtoModel.getExtendedDTO() != null) {
            list.addAll(getFields(apiModel.getDtos().get(dtoModel.getExtendedDTO().getClassName()), apiModel));
        }

        list.addAll(dtoModel.getFields()
                .entrySet()
                .stream()
                .map(fieldModel -> mapField(fieldModel, dtoModel.isJavaRecord()))
                .collect(Collectors.toList()));

        return list;
    }

    private DTOField mapField(Map.Entry<String, ApiTypeModel> fieldModel, boolean javaRecord) {
        var name = fieldModel.getKey();
        var type = fieldModel.getValue();
        String getter;

        if (javaRecord) {
            getter = name;
        } else if (type.getClassName() == null && "boolean".equals(type.getName())) {
            getter = "is" + upperFirstChar(name);
        } else {
            getter = "get" + upperFirstChar(name);
        }

        return new DTOField(mapType(type), name, getter);
    }

    private DTOFieldType mapType(ApiTypeModel type) {
        if (type.getType() == ApiTypeType.GENERIC_TYPE) {
            return genericType(type.getName());
        }

        if (type.getType() == ApiTypeType.UNKNOWN) {
            return objectType(JAVA_LANG_PACKAGE, "Object", List.of());
        }

        var generics = type.getTypeArguments()
                .stream()
                .map(this::mapType)
                .collect(Collectors.toList());
        if (type.getType() == ApiTypeType.DTO || type.getType() == ApiTypeType.ENUM) {
            return objectType(packageName(type.getClassName()), type.getName(), generics);
        }

        if ("collection".equals(type.getName())) {
            return objectType("java.util", "Collection", generics);
        }

        if ("map".equals(type.getName())) {
            return objectType("java.util", "Map", generics);
        }

        if ("array".equals(type.getName())) {
            return arrayType(generics.get(0));
        }

        if ("string".equals(type.getName())) {
            return objectType(JAVA_LANG_PACKAGE, "String", Collections.emptyList());
        }

        if ("number".equals(type.getName())) {
            var i = type.getClassName().lastIndexOf('.');
            return objectType(JAVA_LANG_PACKAGE, type.getClassName().substring(i + 1), Collections.emptyList());
        }

        if ("int".equals(type.getName())) {
            return type.isRequired() ? primaryType("int", "Integer") : objectType(JAVA_LANG_PACKAGE, "Integer", Collections.emptyList());
        }

        return type.isRequired() ? primaryType(type.getName(), upperFirstChar(type.getName())) : objectType(JAVA_LANG_PACKAGE, upperFirstChar(type.getName()), Collections.emptyList());
    }

    private File createPackageDirectory(String packageName) throws IOException {
        var folder = new File(outputDirectory, packageName.replace('.', File.separatorChar));
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Failed to create package " + packageName + " in directory " + outputDirectory);
        }
        return folder;
    }

    private String packageName(String className) {
        var i = className.lastIndexOf('.');
        if (i == -1) {
            throw new IllegalArgumentException("Failed to extract package name from class name " + className);
        }
        return className.substring(0, i);
    }

    private String lowerFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    private String upperFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private Stream<String> streamImports(List<DTOField> fields, String packageName) {
        var checkedClasses = new HashSet<String>();
        return fields.stream()
                .map(DTOField::getType)
                .flatMap(type -> streamImports(type, packageName, checkedClasses));
    }

    private Stream<String> streamImports(DTOFieldType type, String packageName, Set<String> checkedClasses) {
        if (type.getPackageName() == null || JAVA_LANG_PACKAGE.equals(type.getPackageName()) || packageName.equals(type.getPackageName())) {
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
