package de.devx.project.spring.webmvc.test.maven;

import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.maven.io.MavenJavaFileGenerator;
import de.devx.project.commons.maven.parser.MavenSourceFileParser;
import de.devx.project.spring.webmvc.test.generator.SpringWebMvcTestGenerator;
import de.devx.project.spring.webmvc.test.generator.data.SpringWebMvcTestModel;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.List;

@Mojo(
        name = "generate-assertj-entity-assertions",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class SpringWebMvcTestMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/spring-webmvc-tests", required = true)
    private String outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().debug("Creating spring webmvc test models...");
        var models = createModels();

        getLog().debug("Generation spring webmvc for model...");
        var generator = new SpringWebMvcTestGenerator(new MavenJavaFileGenerator(outputDirectory));
        try {
            for (var model : models) {
                generator.generate(model);
            }
            project.addTestCompileSourceRoot(outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate spring webmvc test.", e);
        }
    }

    private List<SpringWebMvcTestModel> createModels() throws MojoExecutionException {
        try {
            var parser = new MavenSourceFileParser(project);
            return parser.getClassesAnnotatedWith("SpringWebMvcTest")
                    .stream()
                    .map(this::mapToModel)
                    .toList();

        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to create spring webmvc models.", ex);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new MojoExecutionException("Failed to create spring webmvc models.", t);
        }
    }

    private SpringWebMvcTestModel mapToModel(JavaClassModel testClass) {
        var annotation = testClass.getAnnotation("SpringWebMvcTest").orElseThrow();
        var model = new SpringWebMvcTestModel();

        return model;
    }
}
