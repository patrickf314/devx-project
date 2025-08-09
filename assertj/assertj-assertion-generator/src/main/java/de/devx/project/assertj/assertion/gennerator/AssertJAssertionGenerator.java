package de.devx.project.assertj.assertion.gennerator;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertFieldModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertionModel;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import de.devx.project.commons.generator.model.JavaTypeArgumentModel;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.devx.project.commons.generator.utils.ImportUtils.asJavaImport;
import static de.devx.project.commons.generator.utils.ImportUtils.combineJavaImports;

public class AssertJAssertionGenerator {

    private final SourceFileGenerator fileGenerator;
    private final Configuration configuration;

    public AssertJAssertionGenerator(SourceFileGenerator fileGenerator) {
        this.fileGenerator = fileGenerator;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(AssertJAssertionGenerator.class.getClassLoader(), "templates");
    }

    public void generateAssertions(AssertJAssertionModel model) throws IOException {
        var asserts = model.getAsserts()
                .stream()
                .collect(Collectors.toMap(
                        assertModel -> assertModel.getPackageName() + "." + "Abstract" + assertModel.getAssertName(),
                        Function.identity()
                ));

        for (var assertModel : model.getAsserts()) {
            generateAbstractAssert(asserts, assertModel);
            generateAssert(assertModel);
        }

        try (var writer = fileGenerator.createSourceFile(model.getPackageName(), model.getName())) {
            var imports = getImports(model, Stream.of(
                    "org.assertj.core.api.Assertions",
                    "org.assertj.core.api.InstanceOfAssertFactory"
            ));
            configuration.getTemplate("AssertjAssertions.ftl").process(Map.of(
                    "model", model,
                    "imports", imports
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    public void generateAssert(AssertJAssertModel model) throws IOException {
        try (var writer = fileGenerator.createSourceFile(model.getPackageName(), model.getAssertName())) {
            configuration.getTemplate("AssertjAssert.ftl").process(Map.of(
                    "model", model
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    public void generateAbstractAssert(Map<String, AssertJAssertModel> models, AssertJAssertModel model) throws IOException {
        try (var writer = fileGenerator.createSourceFile(model.getPackageName(), "Abstract" + model.getAssertName())) {
            var imports = getImports(model, Stream.of(
                    "org.assertj.core.description.TextDescription",
                    "org.assertj.core.error.MultipleAssertionsError",
                    "java.util.List",
                    "java.util.Objects",
                    "java.util.function.Consumer"
            ));
            configuration.getTemplate("AbstractAssertjAssert.ftl").process(Map.of(
                    "model", model,
                    "imports", imports,
                    "fields", model.getFields()
                            .stream()
                            .filter(field -> {
                                var extended = model.getExtendedAbstractAssertModel();
                                var packageName = extended.getPackageName();
                                if (packageName.isEmpty()) {
                                    return true;
                                }

                                var extendedModel = models.get(packageName.get() + "." + extended.getName());
                                if (extendedModel == null) {
                                    return true;
                                }

                                return extendedModel.getFields()
                                        .stream()
                                        .noneMatch(otherField -> field.getName().equals(otherField.getName()));
                            })
                            .toList()
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private List<String> getImports(AssertJAssertModel model, Stream<String> additionalImports) {
        return combineJavaImports(
                model.getFields()
                        .stream()
                        .map(AssertJAssertFieldModel::getType)
                        .flatMap(type -> type.streamImports(model.getPackageName())),
                model.getExtendedAbstractAssertModel().streamImports(model.getPackageName()),
                additionalImports
        );
    }

    private List<String> getImports(AssertJAssertionModel model, Stream<String> additionalImports) {
        return combineJavaImports(
                model.getAsserts()
                        .stream()
                        .flatMap(m -> combineJavaImports(
                                m.asJavaImport(model.getPackageName()),
                                asJavaImport(model.getPackageName(), m.getPackageName(), m.getAssertName()).stream()
                        ).stream()),
                model.getAssertThatMethods()
                        .stream()
                        .flatMap(m -> combineJavaImports(
                                m.getType().streamImports(model.getPackageName()),
                                m.getAssertType().streamImports(model.getPackageName()),
                                m.getTypeArguments()
                                        .stream()
                                        .map(JavaTypeArgumentModel::getTypeConstraint)
                                        .flatMap(Optional::stream)
                                        .flatMap(typeModel -> typeModel.streamImports(m.getName())),
                                asJavaImport(model.getPackageName(), m.getAssertionClass()).stream()
                        ).stream()),
                additionalImports
        );
    }
}
