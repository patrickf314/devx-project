package de.devx.project.hamcrest.matcher.generator;

import de.devx.project.commons.generator.io.SourceFileGenerator;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestClassFieldTypeModel;
import de.devx.project.hamcrest.matcher.generator.data.HamcrestMatcherModel;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class HamcrestMatcherGenerator {

    private static final String JAVA_LANG_PACKAGE = "java.lang";

    private final SourceFileGenerator fileGenerator;
    private final Configuration configuration;

    public HamcrestMatcherGenerator(SourceFileGenerator fileGenerator) {
        this.fileGenerator = fileGenerator;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(HamcrestMatcherGenerator.class.getClassLoader(), "templates");
    }

    public void generate(HamcrestMatcherModel matcher) throws IOException {
        try (var writer = fileGenerator.createSourceFile(matcher.getPackageName(), matcher.getClassName() + "Matcher")) {
            var imports = getImports(matcher);
            configuration.getTemplate("HamcrestMatcher.ftl").process(Map.of(
                    "matcher", matcher,
                    "imports", imports,
                    "matcherFactoryFunctionName", lowerFirstChar(matcher.getClassName())
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private List<String> getImports(HamcrestMatcherModel matcher) {
        var checkedClasses = new HashSet<String>();
        return matcher.getFields()
                .stream()
                .map(HamcrestClassFieldModel::getType)
                .flatMap(type -> streamImports(type, matcher.getPackageName(), checkedClasses))
                .distinct()
                .sorted()
                .toList();
    }

    private Stream<String> streamImports(HamcrestClassFieldTypeModel type, String packageName, Set<String> checkedClasses) {
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

    private String lowerFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
