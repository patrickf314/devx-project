package de.devx.project.assertj.assertion.gennerator;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertFieldModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertionModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJFactoryDefinition;
import de.devx.project.commons.generator.io.SourceFileGenerator;
import de.devx.project.commons.generator.model.JavaTypeModel;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            var additionalImports = Stream.of("org.assertj.core.api.Assertions");

            if (model.getAsserts().stream().anyMatch(a -> a.getTypeArguments().isEmpty()) || !model.getFactoryFields().isEmpty()) {
                additionalImports = Stream.concat(additionalImports, Stream.of("org.assertj.core.api.InstanceOfAssertFactory"));
            }

            var types = createTypeNameResolver(model, additionalImports);
            configuration.getTemplate("AssertjAssertions.ftl").process(Map.of(
                    "model", model,
                    "imports", types.getImports(),
                    "types", types,
                    "factories", createFactoryDefinitions(model, types)
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

    private List<AssertJFactoryDefinition> createFactoryDefinitions(AssertJAssertionModel model, JavaTypeNameResolver types) {
        var asserts = model.getAsserts()
                .stream()
                .filter(a -> a.getTypeArguments().isEmpty())
                .toList();
        var factoryFields = List.copyOf(model.getFactoryFields());

        var factories = new ArrayList<AssertJFactoryNameResolver.Factory>();
        asserts.forEach(a -> factories.add(new AssertJFactoryNameResolver.Factory(
                a.getFactoryName(), a.getPackageName(), Optional.empty())));
        factoryFields.forEach(f -> factories.add(new AssertJFactoryNameResolver.Factory(
                f.getFieldName(),
                f.getAssertedType().flatMap(JavaTypeModel::getPackageName).orElse(""),
                f.getPrefix())));
        var names = AssertJFactoryNameResolver.resolve(factories);

        var definitions = new ArrayList<AssertJFactoryDefinition>();
        for (var i = 0; i < asserts.size(); i++) {
            var a = asserts.get(i);
            definitions.add(new AssertJFactoryDefinition(
                    names.get(i),
                    a.getName(),
                    "InstanceOfAssertFactory<" + a.getName() + ", " + a.getAssertName() + ">",
                    "new InstanceOfAssertFactory<>(" + a.getName() + ".class, " + model.getName() + "::assertThat)"
            ));
        }
        for (var i = 0; i < factoryFields.size(); i++) {
            var f = factoryFields.get(i);
            definitions.add(new AssertJFactoryDefinition(
                    names.get(asserts.size() + i),
                    f.getAssertedType().map(t -> rawType(types, t)).orElse("Object"),
                    types.type(f.getType()),
                    types.className(f.getAssertionClass()) + "." + f.getFieldName()
            ));
        }
        return definitions;
    }

    private static String rawType(JavaTypeNameResolver types, JavaTypeModel type) {
        return type.getPackageName()
                .map(p -> types.type(JavaTypeModel.objectType(p, type.getName())))
                .orElse(type.getName());
    }

    private JavaTypeNameResolver createTypeNameResolver(AssertJAssertionModel model, Stream<String> additionalImports) {
        var requiredTypes = Stream.concat(
                additionalImports,
                model.getAsserts()
                        .stream()
                        .flatMap(m -> Stream.concat(
                                Stream.of(m.getPackageName() + "." + m.getRootName(), m.getPackageName() + "." + m.getAssertName()),
                                m.getTypeArguments()
                                        .stream()
                                        .flatMap(t -> t.getTypeConstraint().stream())
                                        .flatMap(JavaTypeNameResolver::streamQualifiedNames)
                        ))
        ).toList();

        var assertThatTypes = model.getAssertThatMethods()
                .stream()
                .flatMap(m -> Stream.of(
                        JavaTypeNameResolver.streamQualifiedNames(m.getType()),
                        JavaTypeNameResolver.streamQualifiedNames(m.getAssertType()),
                        m.getTypeArguments()
                                .stream()
                                .flatMap(t -> t.getTypeConstraint().stream())
                                .flatMap(JavaTypeNameResolver::streamQualifiedNames),
                        Stream.of(m.getAssertionClass().getFullyQualifiedName())
                ).flatMap(Function.identity()));

        var factoryTypes = model.getFactoryFields()
                .stream()
                .flatMap(f -> Stream.concat(
                        JavaTypeNameResolver.streamQualifiedNames(f.getType()),
                        Stream.of(f.getAssertionClass().getFullyQualifiedName())
                ));

        var optionalTypes = Stream.concat(assertThatTypes, factoryTypes).toList();

        return new JavaTypeNameResolver(model.getPackageName(), model.getName(), requiredTypes, optionalTypes);
    }
}
