package de.devx.project.assertj.assertion.maven;

import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertModel;
import de.devx.project.assertj.assertion.gennerator.data.AssertJAssertThatMethodModel;
import de.devx.project.assertj.assertion.gennerator.mapper.AssertJAssertMapper;
import de.devx.project.commons.maven.logging.MavenLogger;
import de.devx.project.commons.maven.parser.MavenSourceFileParser;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Mojo(
        name = "generate-assertj-entity-assertions",
        defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
)
public class AssertJEntityAssertionsMojo extends AbstractAssertJAssertionsMojo {

    private static final AssertJAssertMapper MAPPER = Mappers.getMapper(AssertJAssertMapper.class);

    protected List<AssertJAssertModel> createAssertModels() throws MojoExecutionException {
        getLog().debug("Creating assert models...");

        try {
            var parser = new MavenSourceFileParser(project, new MavenLogger(getLog()));
            return Stream.concat(
                            parser.getClassesAnnotatedWith("MappedSuperclass").stream(),
                            parser.getClassesAnnotatedWith("Entity").stream()
                    )
                    .map(MAPPER::mapToAssert)
                    .toList();
        } catch (IOException | DependencyResolutionRequiredException ex) {
            throw new MojoExecutionException("Failed to create assert models.", ex);
        }
    }

    @Override
    protected List<AssertJAssertThatMethodModel> createAssertThatMethodModels() {
        return Collections.emptyList();
    }
}
