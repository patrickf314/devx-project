package api.maven.plugin.angular.client;

import api.maven.plugin.angular.client.data.TypeScriptTypeAlias;
import api.maven.plugin.core.io.JarApiModelExtractor;
import api.maven.plugin.core.model.ApiModel;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mojo(
        name = "generate-angular-client",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
public class AngularClientGeneratorMojo extends AbstractMojo {

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject mavenProject;
    @Parameter(defaultValue = "${project.build.directory}")
    private String outputDirectory;
    @Parameter
    private List<TypeScriptTypeAlias> typeScriptTypeAliases;

    @Override
    public void execute() throws MojoExecutionException {
        if (typeScriptTypeAliases == null) {
            typeScriptTypeAliases = Collections.emptyList();
        }

        getLog().info("Starting generation of API clients...");
        getLog().debug("Searing for api model definition in dependencies...");

        var apiModels = mavenProject.getArtifacts()
                .stream()
                .map(Artifact::getFile)
                .map(this::extract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        getLog().debug("Found " + apiModels.size() + " api model(s) in dependencies, start client generations");

        try {
            createClient(apiModels);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate client code", e);
        }

        getLog().info("API client generation completed");
    }

    private void createClient(List<ApiModel> apiModels) throws IOException {
        var generator = new AngularClientGenerator(outputDirectory, typeScriptTypeAliases);
        for(var apiModel : apiModels) {
            generator.generate(apiModel);
        }
    }

    private Optional<ApiModel> extract(File file) {
        if (file.getName().endsWith(".jar")) {
            return new JarApiModelExtractor().extract(file);
        } else {
            getLog().warn("Cannot extract artifact file " + file.getAbsolutePath());
            return Optional.empty();
        }
    }
}
