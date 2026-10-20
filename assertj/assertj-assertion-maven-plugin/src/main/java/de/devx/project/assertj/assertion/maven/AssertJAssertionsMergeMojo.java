package de.devx.project.assertj.assertion.maven;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertThatMethodModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJFactoryFieldModel;
import de.devx.project.commons.generator.model.JavaClassFieldModel;
import de.devx.project.commons.generator.model.JavaClassMethodModel;
import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.generator.model.JavaTypeModel;
import de.devx.project.commons.maven.logging.MavenLogger;
import de.devx.project.commons.maven.parser.MavenProjectClassLoader;
import de.devx.project.commons.maven.parser.MavenSourceFileParser;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mojo(
        name = "merge-assertj-assertions",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class AssertJAssertionsMergeMojo extends AbstractAssertJAssertionsMojo {

    private static final String INSTANCE_OF_ASSERT_FACTORY_PACKAGE = "org.assertj.core.api";
    private static final String INSTANCE_OF_ASSERT_FACTORY = "InstanceOfAssertFactory";

    @Parameter(required = true)
    private List<String> assertions;

    /**
     * Prefixes for {@code InstanceOfAssertFactory} fields of the merged assertion classes,
     * keyed by the fully qualified name of the assertion class.
     * A prefix is only applied if the name of a factory clashes with another one.
     * By default, the prefix is derived from the package of the asserted type.
     */
    @Parameter
    private Map<String, String> factoryPrefixes = new HashMap<>();

    private List<JavaClassModel> assertionClasses;

    @Override
    protected Set<AssertJAssertModel> createAssertModels() {
        return Collections.emptySet();
    }

    @Override
    protected Set<AssertJAssertThatMethodModel> createAssertThatMethodModels() throws MojoExecutionException {
        var models = new LinkedHashSet<AssertJAssertThatMethodModel>();
        for (var assertionClass : getAssertionClasses()) {
            var childModels = assertionClass.getMethods()
                    .stream()
                    .filter(JavaClassMethodModel::isStatic)
                    .filter(m -> m.getName().startsWith("assert") && m.getParameterCount() == 1)
                    .map(m -> mapToModel(assertionClass, m))
                    .toList();
            models.addAll(childModels);
            getLog().debug("Found " + childModels.size() + " assert functions in " + assertionClass.getFullyQualifiedName());
        }
        return models;
    }

    @Override
    protected Set<AssertJFactoryFieldModel> createFactoryFieldModels() throws MojoExecutionException {
        var models = new LinkedHashMap<String, AssertJFactoryFieldModel>();
        for (var assertionClass : getAssertionClasses()) {
            assertionClass.getFields()
                    .stream()
                    .filter(field -> field.isStatic() && field.isPublic())
                    .filter(field -> isInstanceOfAssertFactory(field.getType()))
                    .map(field -> mapToModel(assertionClass, field))
                    // several merged classes might export a factory for the same type, e.g. if they are merged themselves
                    .forEach(model -> models.putIfAbsent(model.getType().getQualifiedTypeName(), model));
        }
        return new LinkedHashSet<>(models.values());
    }

    private static boolean isInstanceOfAssertFactory(JavaTypeModel type) {
        return type.getPackageName().filter(INSTANCE_OF_ASSERT_FACTORY_PACKAGE::equals).isPresent()
                && INSTANCE_OF_ASSERT_FACTORY.equals(type.getName())
                && !type.isArray();
    }

    private List<JavaClassModel> getAssertionClasses() throws MojoExecutionException {
        if (assertionClasses == null) {
            try {
                var parser = new MavenSourceFileParser(project, new MavenLogger(getLog()));
                var classLoader = new MavenProjectClassLoader(project);
                assertionClasses = new ArrayList<>();
                for (var assertion : assertions) {
                    getLog().info("Loading assertion: " + assertion);
                    assertionClasses.add(parser.getClass(assertion).orElseGet(() -> classLoader.getClass(assertion)));
                }
            } catch (IOException | DependencyResolutionRequiredException e) {
                throw new MojoExecutionException("Failed to initialize java parser", e);
            }
        }
        return assertionClasses;
    }

    private AssertJFactoryFieldModel mapToModel(JavaClassModel assertionClass, JavaClassFieldModel field) {
        var model = new AssertJFactoryFieldModel();
        model.setAssertionClass(assertionClass);
        model.setFieldName(field.getName());
        model.setType(field.getType());
        model.setPrefix(factoryPrefixes.get(assertionClass.getFullyQualifiedName()));
        return model;
    }

    private AssertJAssertThatMethodModel mapToModel(JavaClassModel assertionClass, JavaClassMethodModel methodDeclaration) {
        var model = new AssertJAssertThatMethodModel();
        model.setName(methodDeclaration.getName());
        model.setAssertionClass(assertionClass);
        model.setType(methodDeclaration.getParameters().get(0).getType());
        model.setAssertType(methodDeclaration.getReturnType());
        model.setTypeArguments(methodDeclaration.getTypeArguments());
        return model;
    }
}
