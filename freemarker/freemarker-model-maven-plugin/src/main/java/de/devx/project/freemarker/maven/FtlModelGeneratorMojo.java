package de.devx.project.freemarker.maven;

import de.devx.project.commons.maven.io.MavenJavaFileGenerator;
import de.devx.project.freemarker.generator.FtlModelGenerator;
import de.devx.project.freemarker.generator.parser.FtlTemplateParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

@Mojo(
        name = "generate-models",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true
)
public class FtlModelGeneratorMojo extends AbstractMojo {

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/ftl-models")
    private String outputDirectory;

    @Parameter(defaultValue = "${project.build.resources[0].directory}")
    private File resourcesDirectory;

    @Parameter
    private String include;

    @Parameter(required = true)
    private String outputPackage;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting generation of FTL model classes...");

        try {
            var resourcesPath = resourcesDirectory.toPath();
            var scanPath = include != null && !include.isBlank() ? resourcesPath.resolve(include) : resourcesPath;

            var parser = new FtlTemplateParser();
            var templates = parser.parseTemplates(resourcesPath, scanPath, outputPackage);

            var modelsToGenerate = templates.stream()
                    .filter(t -> !t.getVariables().isEmpty())
                    .toList();

            if (modelsToGenerate.isEmpty()) {
                getLog().info("No FTL templates with variables found in: " + scanPath);
                return;
            }

            var generator = new FtlModelGenerator(new MavenJavaFileGenerator(outputDirectory));
            for (var template : modelsToGenerate) {
                getLog().debug("Generating model for: " + template.getTemplateName() + ".ftl");
                generator.generateModel(template);
            }

            generator.generateFreemarkerTemplate(outputPackage, modelsToGenerate);
            generator.generateFreemarkerTemplateEngine(outputPackage);

            mavenProject.addCompileSourceRoot(outputDirectory);
            getLog().info("FTL model generation completed.");
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate FTL model classes", e);
        }
    }
}
