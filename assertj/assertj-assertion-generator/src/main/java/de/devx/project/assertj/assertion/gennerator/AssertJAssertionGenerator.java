package de.devx.project.assertj.assertion.gennerator;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertFieldModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertionModel;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
        for (var assertModel : model.getAsserts()) {
            generateAbstractAssert(assertModel);
            generateAssert(assertModel);
        }

        try (var writer = fileGenerator.createSourceFile(model.getPackageName(), model.getName())) {
            var imports = getImports(model, Stream.of(
                    "org.assertj.core.api.Assertions"
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
        try (var writer = fileGenerator.createSourceFile(model.getPackageName(), model.getName() + "Assert")) {
            configuration.getTemplate("AssertjAssert.ftl").process(Map.of(
                    "model", model
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    public void generateAbstractAssert(AssertJAssertModel model) throws IOException {
        try (var writer = fileGenerator.createSourceFile(model.getPackageName(), "Abstract" + model.getName() + "Assert")) {
            var imports = getImports(model, Stream.of(
                    "org.assertj.core.api.AbstractAssert",
                    "org.assertj.core.description.TextDescription",
                    "org.assertj.core.error.MultipleAssertionsError",
                    "java.util.List",
                    "java.util.Objects",
                    "java.util.function.Consumer"
            ));
            configuration.getTemplate("AbstractAssertjAssert.ftl").process(Map.of(
                    "model", model,
                    "imports", imports
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
                additionalImports
        );
    }

    private List<String> getImports(AssertJAssertionModel model, Stream<String> additionalImports) {
        return combineJavaImports(
                model.getAsserts()
                        .stream()
                        .flatMap(m -> combineJavaImports(
                                m.asJavaImport(model.getPackageName()).stream(),
                                asJavaImport(model.getPackageName(), m.getPackageName(), m.getName() + "Assert").stream()
                        ).stream()),
                model.getAssertThatMethods()
                        .stream()
                        .flatMap(m -> combineJavaImports(
                                m.getType().streamImports(model.getPackageName()),
                                m.getAssertType().streamImports(model.getPackageName()),
                                asJavaImport(model.getPackageName(), m.getAssertionClass()).stream()
                        ).stream()),
                additionalImports
        );
    }
}
