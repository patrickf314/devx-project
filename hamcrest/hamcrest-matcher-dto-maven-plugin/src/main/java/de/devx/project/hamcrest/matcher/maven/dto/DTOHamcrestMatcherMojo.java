package de.devx.project.hamcrest.matcher.maven.dto;

import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.io.JarApiModelExtractor;
import de.devx.project.commons.maven.io.MavenSourceFileGenerator;
import de.devx.project.hamcrest.matcher.generator.HamcrestMatcherGenerator;
import de.devx.project.hamcrest.matcher.maven.dto.mapper.DTOHamcrestMatcherMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.mapstruct.factory.Mappers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Mojo(
        name = "generate-dto-hamcrest-matchers",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
@AllArgsConstructor
@NoArgsConstructor
public class DTOHamcrestMatcherMojo extends AbstractMojo {

    private static final DTOHamcrestMatcherMapper MAPPER = Mappers.getMapper(DTOHamcrestMatcherMapper.class);

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject mavenProject;
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/test-annotations")
    private String outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting generation DTO hamcrest matchers...");
        getLog().debug("Searing for api model definition in dependencies...");

        var apiModels = mavenProject.getArtifacts()
                .stream()
                .map(Artifact::getFile)
                .map(this::extract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        getLog().debug("Found " + apiModels.size() + " api model(s) in dependencies, start DTO hamcrest matcher generations");

        try {
            createMatchers(apiModels);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate hamcrest matchers", e);
        }

        getLog().info("DTO hamcrest matcher generation completed");
    }

    private void createMatchers(List<ApiModel> apiModels) throws IOException {
        var fileGenerator = new MavenSourceFileGenerator(outputDirectory);
        var generator = new HamcrestMatcherGenerator(fileGenerator);

        var matchers = apiModels.stream()
                .map(MAPPER::mapApiDTOs)
                .flatMap(List::stream)
                .toList();

        for (var matcher : matchers) {
            generator.generate(matcher);
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
