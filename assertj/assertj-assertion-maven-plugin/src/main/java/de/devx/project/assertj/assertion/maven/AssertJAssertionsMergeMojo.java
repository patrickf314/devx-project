package de.devx.project.assertj.assertion.maven;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertThatMethodModel;
import de.devx.project.commons.generator.model.JavaClassMethodModel;
import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.maven.parser.MavenSourceFileParser;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    protected List<AssertJAssertModel> createAssertModels() {
        return Collections.emptyList();
    }

    @Override
    protected List<AssertJAssertThatMethodModel> createAssertThatMethodModels() throws MojoExecutionException {
        try {
            var models = new ArrayList<AssertJAssertThatMethodModel>();
            var parser = new MavenSourceFileParser(project);
            for (var assertion : assertions) {
                models.addAll(createAssertThatMethodModels(assertion, parser));
            }
            return models;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to initialize java parser", e);
        } catch (Throwable e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<AssertJAssertThatMethodModel> createAssertThatMethodModels(String assertion, MavenSourceFileParser parser) throws MojoExecutionException {
        var assertionClass = parser.getClass(assertion);

        return assertionClass.getMethods()
                .stream()
                .filter(JavaClassMethodModel::isStatic)
                .filter(m -> m.getName().equals("assertThat") && m.getParameterCount() == 1)
                .map(m -> mapToModel(assertionClass, m))
                .toList();
    }

    private AssertJAssertThatMethodModel mapToModel(JavaClassModel assertionClass, JavaClassMethodModel methodDeclaration) {
        var model = new AssertJAssertThatMethodModel();
        model.setAssertionClass(assertionClass);
        model.setType(methodDeclaration.getParameters().get(0).getType());
        model.setAssertType(methodDeclaration.getReturnType());
        model.setTypeArguments(model.getTypeArguments());
        return model;
    }
}
