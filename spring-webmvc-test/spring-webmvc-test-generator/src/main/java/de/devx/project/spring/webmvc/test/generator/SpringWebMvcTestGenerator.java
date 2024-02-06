package de.devx.project.spring.webmvc.test.generator;

import de.devx.project.commons.generator.io.SourceFileGenerator;
import de.devx.project.spring.webmvc.test.generator.data.*;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            var randomFunctions = getRandomFunctions(test, parametrizedRandom);

            configuration.getTemplate("SpringWebMvcTest.ftl").process(Map.of(
                    "test", test,
                    "imports", getImports(test),
                    "parametrizedRandom", parametrizedRandom.values(),
                    "randomFunctions", randomFunctions
            ), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private Map<String, String> getRandomFunctions(SpringWebMvcTestModel model, Map<SpringWebMvcTypeModel, SpringWebMvcParametrizedRandomModel> parametrizedRandom) {
        return model.getMethods().stream()
                .flatMap(this::streamTypes)
                .distinct()
                .collect(Collectors.toMap(Object::toString, type -> getRandomFunction(type, parametrizedRandom)));
    }

    private String getRandomFunction(SpringWebMvcTypeModel model, Map<SpringWebMvcTypeModel, SpringWebMvcParametrizedRandomModel> parametrizedRandomMap) {
        var parametrizedRandom = parametrizedRandomMap.get(model);
        if (parametrizedRandom != null) {
            return parametrizedRandom.getName() + "()";
        }

        return switch (model.getName()) {
            case "int" -> "random.nextInt()";
            case "double" -> "random.nextDouble()";
            case "float" -> "random.nextFloat()";
            case "boolean" -> "random.nextBoolean()";
            default -> "random.nextObject(" + model.getName() + ".class)";
        };
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
        var parametrizedRandomMethodsWithSameName = counter.compute(model.getName(), (ignore, c) -> c == null ? 1 : c + 1);
        if (parametrizedRandomMethodsWithSameName == 1) {
            return new SpringWebMvcParametrizedRandomModel("next" + model.getName(), model);
        } else {
            return new SpringWebMvcParametrizedRandomModel("next" + model.getName() + parametrizedRandomMethodsWithSameName, model);
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
        if (model.getPackageName() == null || "java.lang".equals(model.getPackageName())) {
            return Stream.empty();
        }

        return Stream.concat(
                Stream.of(model).filter(m -> !m.getPackageName().equals(currentPackage)).map(m -> m.getPackageName() + "." + m.getName()),
                model.getGenerics().stream().flatMap(generic -> asImport(currentPackage, generic))
        );
    }
}
