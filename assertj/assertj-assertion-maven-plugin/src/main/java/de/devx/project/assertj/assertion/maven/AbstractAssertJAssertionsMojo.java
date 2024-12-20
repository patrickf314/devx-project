package de.devx.project.assertj.assertion.maven;

import de.devx.project.assertj.assertion.gennerator.AssertJAssertionGenerator;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertThatMethodModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertionModel;
import de.devx.project.commons.generator.model.JavaClassModel;
import de.devx.project.commons.maven.io.MavenJavaFileGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.List;

public abstract class AbstractAssertJAssertionsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/assertj-assertions", required = true)
    private String outputDirectory;

    @Parameter(required = true)
    private String packageName;
    @Parameter(required = true)
    private String name;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            getLog().info("Starting generation of assertJ assertions...");

            var model = new AssertJAssertionModel();
            model.setPackageName(packageName);
            model.setName(name);
            model.setAsserts(createAssertModels());
            model.setAssertThatMethods(createAssertThatMethodModels());

            generateAssertions(model);
            getLog().info("Generation of assertJ assertions completed.");
        }catch (RuntimeException e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed to execute AssertJ mojo.", e);
        }
    }

    protected abstract List<AssertJAssertModel> createAssertModels() throws MojoExecutionException;

    protected abstract List<AssertJAssertThatMethodModel> createAssertThatMethodModels() throws MojoExecutionException;

    private void generateAssertions(AssertJAssertionModel model) throws MojoExecutionException {
        getLog().debug("Generation assertions for model...");

        try {
            var generator = new AssertJAssertionGenerator(new MavenJavaFileGenerator(outputDirectory));
            generator.generateAssertions(model);
            project.addTestCompileSourceRoot(outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate assertions.", e);
        }
    }
}
