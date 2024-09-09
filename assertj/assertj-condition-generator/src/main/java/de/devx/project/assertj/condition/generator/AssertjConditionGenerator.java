package de.devx.project.assertj.condition.generator;

import de.devx.project.assertj.condition.generator.data.AssertjClassFieldModel;
import de.devx.project.assertj.condition.generator.data.AssertjClassFieldTypeModel;
import de.devx.project.assertj.condition.generator.data.AssertjConditionModel;
import freemarker.template.Configuration;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AssertjConditionGenerator {

    private static final String JAVA_LANG_PACKAGE = "java.lang";

    private final SourceFileGenerator fileGenerator;
    private final Configuration configuration;

    public AssertjConditionGenerator(SourceFileGenerator fileGenerator) {
        this.fileGenerator = fileGenerator;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(AssertjConditionGenerator.class.getClassLoader(), "templates");
    }

    public void generate(AssertjConditionModel condition) throws IOException {
        if (condition.getEnclosingDTO() != null) {
            return;
        }

        try (var writer = fileGenerator.createSourceFile(condition.getPackageName(), condition.getClassName() + "Condition")) {
            var imports = getImports(condition);
            configuration.getTemplate("AssertjCondition.ftl").process(Map.of(
                    "condition", condition,
                    "imports", imports,
                    "conditionFactoryFunctionName", lowerFirstChar(condition.getClassName())
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private List<String> getImports(AssertjConditionModel condition) {
        return condition.getFields()
                .stream()
                .map(AssertjClassFieldModel::getType)
                .flatMap(type -> streamImports(type, condition.getPackageName()))
                .distinct()
                .sorted()
                .toList();
    }

    private Stream<String> streamImports(AssertjClassFieldTypeModel type, String packageName) {
        if (type.getPackageName() == null || JAVA_LANG_PACKAGE.equals(type.getPackageName())) {
            return Stream.empty();
        }

        var fullName = packageName.equals(type.getPackageName()) ? Stream.<String>empty() : Stream.of(type.getPackageName() + "." + type.getClassName());
        var generics = type.getGenerics()
                .stream()
                .flatMap(genericType -> streamImports(genericType, packageName));

        return Stream.concat(fullName, generics);
    }

    private String lowerFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
