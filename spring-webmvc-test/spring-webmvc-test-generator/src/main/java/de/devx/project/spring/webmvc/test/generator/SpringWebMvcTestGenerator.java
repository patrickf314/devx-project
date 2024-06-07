package de.devx.project.spring.webmvc.test.generator;

import de.devx.project.commons.generator.io.SourceFileGenerator;
import de.devx.project.spring.webmvc.test.generator.data.*;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringWebMvcTestGenerator {

    private final SourceFileGenerator fileGenerator;
    private final Configuration configuration;

    public SpringWebMvcTestGenerator(SourceFileGenerator fileGenerator) {
        this.fileGenerator = fileGenerator;

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(SpringWebMvcTestGenerator.class.getClassLoader(), "templates");
    }

    public void generate(SpringWebMvcTestModel test) throws IOException {
        try (var writer = fileGenerator.createSourceFile(test.getPackageName(), test.getName())) {
            var parametrizedRandom = getParametrizedRandom(test);

            configuration.getTemplate("SpringWebMvcTest.ftl").process(Map.of(
                    "test", test,
                    "imports", getImports(test),
                    "randomFunctions", new SpringWebMvcRandomFunctionsModel(parametrizedRandom)
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private Map<SpringWebMvcTypeModel, SpringWebMvcParametrizedRandomModel> getParametrizedRandom(SpringWebMvcTestModel model) {
        var counter = new HashMap<String, Integer>();

        return model.getMethods().stream()
                .flatMap(this::streamTypes)
                .distinct()
                .filter(this::isParametrizedRandomMethodRequired)
                .collect(Collectors.toMap(Function.identity(), type -> mapToParametrizedRandom(counter, type)));
    }

    private Stream<SpringWebMvcTypeModel> streamTypes(SpringWebMvcMethodModel model) {
        return Stream.concat(
                Stream.of(model.getReturnType()),
                model.getParameters().stream().map(SpringWebMvcParameterModel::getType)
        );
    }

    private boolean isParametrizedRandomMethodRequired(SpringWebMvcTypeModel model) {
        return !model.getGenerics().isEmpty();
    }

    private SpringWebMvcParametrizedRandomModel mapToParametrizedRandom(Map<String, Integer> counter, SpringWebMvcTypeModel model) {
        var parametrizedRandomMethodsWithSameName = counter.compute(model.getMockName(), (ignore, c) -> c == null ? 1 : c + 1);
        if (parametrizedRandomMethodsWithSameName == 1) {
            return new SpringWebMvcParametrizedRandomModel("next" + model.getMockName(), model);
        } else {
            return new SpringWebMvcParametrizedRandomModel("next" + model.getMockName() + parametrizedRandomMethodsWithSameName, model);
        }
    }

    private Set<String> getImports(SpringWebMvcTestModel model) {
        var packageName = model.getPackageName();

        return Stream.of(
                        asImport(packageName, model.getController()),
                        asImport(packageName, model.getService()),
                        model.getContext().stream().flatMap(context -> asImport(packageName, context)),
                        model.getMethods()
                                .stream()
                                .flatMap(method -> streamImports(packageName, method))
                )
                .flatMap(Function.identity())
                .collect(Collectors.toSet());
    }

    private Stream<String> streamImports(String currentPackage, SpringWebMvcMethodModel model) {
        return Stream.concat(
                asImport(currentPackage, model.getReturnType()),
                model.getParameters().stream().map(SpringWebMvcParameterModel::getType).flatMap(parameter -> asImport(currentPackage, parameter))
        );
    }

    private Stream<String> asImport(String currentPackage, SpringWebMvcTypeModel model) {
        if (model.getMockPackageName() == null || "java.lang".equals(model.getMockPackageName())) {
            return Stream.empty();
        }

        return Stream.concat(
                Stream.of(model).filter(m -> !m.getMockPackageName().equals(currentPackage)).map(m -> m.getMockPackageName() + "." + m.getMockName()),
                model.getGenerics().stream().flatMap(generic -> asImport(currentPackage, generic))
        );
    }
}
