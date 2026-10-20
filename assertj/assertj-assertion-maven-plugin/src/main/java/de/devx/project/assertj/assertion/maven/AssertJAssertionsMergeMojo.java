package de.devx.project.assertj.assertion.maven;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertThatMethodModel;
import de.devx.project.commons.generator.model.JavaClassMethodModel;
import de.devx.project.commons.generator.model.JavaClassModel;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(
        name = "merge-assertj-assertions",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class AssertJAssertionsMergeMojo extends AbstractAssertJAssertionsMojo {

    @Parameter(required = true)
    private List<String> assertions;

    @Override
    protected Set<AssertJAssertModel> createAssertModels() {
        return Collections.emptySet();
    }

    @Override
    protected Set<AssertJAssertThatMethodModel> createAssertThatMethodModels() throws MojoExecutionException {
        try {
            var models = new LinkedHashSet<AssertJAssertThatMethodModel>();
            var parser = new MavenSourceFileParser(project, new MavenLogger(getLog()));
            var classLoader = new MavenProjectClassLoader(project);
            for (var assertion : assertions) {
                getLog().info("Loading assertion: " + assertion);
                var childModels = createAssertThatMethodModels(assertion, parser, classLoader);
                models.addAll(childModels);
                getLog().debug("Found " + childModels.size() + " assert functions:");
            }
            return models;
        } catch (IOException | DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Failed to initialize java parser", e);
        }
    }

    private Set<AssertJAssertThatMethodModel> createAssertThatMethodModels(String assertion, MavenSourceFileParser parser, MavenProjectClassLoader classLoader) {
        var assertionClass = parser.getClass(assertion)
                .orElseGet(() -> classLoader.getClass(assertion));

        return assertionClass.getMethods()
                .stream()
                .filter(JavaClassMethodModel::isStatic)
                .filter(m -> m.getName().startsWith("assert") && m.getParameterCount() == 1)
                .map(m -> mapToModel(assertionClass, m))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
