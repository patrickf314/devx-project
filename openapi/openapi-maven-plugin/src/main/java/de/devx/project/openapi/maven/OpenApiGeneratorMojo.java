package de.devx.project.openapi.maven;

import de.devx.project.commons.maven.parser.MavenApiModelParser;
import de.devx.project.openapi.generator.OpenApiGenerator;
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
import java.util.List;
import java.util.function.Predicate;

@Mojo(
        name = "generate-openapi",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
public class OpenApiGeneratorMojo extends AbstractMojo {

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${project.build.directory}")
    private String outputDirectory;

    @Parameter(property = "openapi.outputFileName", defaultValue = "openapi.yaml")
    private String outputFileName;

    /**
     * Optional explicit path to an {@code api-model.json} file. When set, dependency scanning
     * is skipped and this file is used directly. When omitted (the default), the plugin
     * scans all compile- and runtime-scoped dependencies for an embedded {@code api-model.json}.
     */
    @Parameter(property = "openapi.apiModelJson")
    private File apiModelJson;

    /**
     * Optional list of {@code groupId:artifactId} patterns used to restrict which dependencies
     * are scanned for an {@code api-model.json}. Wildcards ({@code *}) are supported in both
     * positions, e.g. {@code com.example:*} or {@code *:my-api}. When omitted, all
     * compile- and runtime-scoped dependencies are scanned.
     *
     * <pre>{@code
     * <includes>
     *   <include>com.example:my-api</include>
     * </includes>
     * }</pre>
     */
    @Parameter
    private List<String> includes;

    /** Title of the API, used in the OpenAPI info section. */
    @Parameter(required = true)
    private String title;

    /** Description of the API, used in the OpenAPI info section. */
    @Parameter
    private String description;

    /** Version of the API, used in the OpenAPI info section. */
    @Parameter(defaultValue = "${project.version}")
    private String version;

    /** URL of the server, used in the OpenAPI servers section. */
    @Parameter
    private String serverUrl;

    /** Human-readable description of the server. */
    @Parameter
    private String serverDescription;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Generating OpenAPI v3 specification...");

        var parser = new MavenApiModelParser(getLog(), apiModelJson, mavenProject, artifactFilter());
        var apiModels = parser.requireApiModels();

        var outputFile = new File(outputDirectory, outputFileName);
        var generator = new OpenApiGenerator(title, description, version, serverUrl, serverDescription, outputFile);

        try {
            generator.generate(apiModels);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate OpenAPI specification", e);
        }

        getLog().info("OpenAPI specification written to " + outputFile.getAbsolutePath());
    }

    private Predicate<Artifact> artifactFilter() {
        return artifact -> isCompileOrRuntime(artifact) && matchesIncludes(artifact);
    }

    private boolean isCompileOrRuntime(Artifact artifact) {
        var scope = artifact.getScope();
        return Artifact.SCOPE_COMPILE.equals(scope) || Artifact.SCOPE_RUNTIME.equals(scope);
    }

    private boolean matchesIncludes(Artifact artifact) {
        if (includes == null || includes.isEmpty()) {
            return true;
        }
        return includes.stream().anyMatch(pattern -> matchesPattern(artifact, pattern));
    }

    private boolean matchesPattern(Artifact artifact, String pattern) {
        var parts = pattern.split(":", 2);
        if (parts.length != 2) {
            getLog().warn("Invalid include pattern (expected groupId:artifactId): " + pattern);
            return false;
        }
        return matchesSegment(artifact.getGroupId(), parts[0])
                && matchesSegment(artifact.getArtifactId(), parts[1]);
    }

    private boolean matchesSegment(String value, String pattern) {
        return "*".equals(pattern) || value.equals(pattern);
    }
}
