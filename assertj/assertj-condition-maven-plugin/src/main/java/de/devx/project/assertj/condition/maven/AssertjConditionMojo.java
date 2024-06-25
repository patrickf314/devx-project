package de.devx.project.assertj.condition.maven;

import de.devx.project.assertj.condition.generator.AssertjConditionGenerator;
import de.devx.project.assertj.condition.generator.data.AssertjConditionModel;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.io.JarApiModelExtractor;
import de.devx.project.commons.maven.io.MavenJavaFileGenerator;
import de.devx.project.assertj.condition.maven.mapper.AssertjConditionMapper;
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
import java.util.Set;

@Mojo(
        name = "generate-assertj-conditions",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
@AllArgsConstructor
@NoArgsConstructor
public class AssertjConditionMojo extends AbstractMojo {

    private static final AssertjConditionMapper MAPPER = Mappers.getMapper(AssertjConditionMapper.class);

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject mavenProject;
    @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/assertj-conditions")
    private String outputDirectory;

    @Parameter
    private Set<String> packages;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting generation of assertJ conditions...");
        getLog().debug("Searing for api model definition in dependencies...");

        var apiModels = mavenProject.getArtifacts()
                .stream()
                .map(Artifact::getFile)
                .map(this::extract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        getLog().debug("Found " + apiModels.size() + " api model(s) in dependencies, start assertj condition generations");

        try {
            createConditions(apiModels);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate assertj conditions", e);
        }

        mavenProject.addTestCompileSourceRoot(outputDirectory);
        getLog().info("AssertJ condition generation completed");
        getLog().info(outputDirectory);
    }

    private void createConditions(List<ApiModel> apiModels) throws IOException {
        var fileGenerator = new MavenJavaFileGenerator(outputDirectory);
        var generator = new AssertjConditionGenerator(fileGenerator);

        var conditions = apiModels.stream()
                .map(MAPPER::mapApiDTOs)
                .flatMap(List::stream)
                .filter(this::filterCondition)
                .toList();

        for (var condition : conditions) {
            generator.generate(condition);
        }
    }

    private boolean filterCondition(AssertjConditionModel assertjConditionModel) {
        if (packages == null || packages.isEmpty()) {
            return true;
        }

        return packages.stream().anyMatch(prefix -> assertjConditionModel.getPackageName().startsWith(prefix));
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
