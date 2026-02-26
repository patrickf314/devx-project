package de.devx.project.commons.maven.parser;

import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.io.ApiModelFileExtractor;
import de.devx.project.commons.api.model.io.JarApiModelExtractor;
import lombok.RequiredArgsConstructor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class MavenApiModelParser {

    private final Log log;
    private final File apiModelJson;
    private final MavenProject mavenProject;
    private final Predicate<Artifact> artifactFilter;

    public static String extractPackageName(ApiDTOModel apiModel) {
        if (apiModel.getEnclosingDTO() != null) {
            return extractPackageName(apiModel.getEnclosingDTO().getClassName());
        } else {
            return extractPackageName(apiModel.getClassName());
        }
    }

    private static String extractPackageName(String fullyQualifiedClassName) {
        var i = fullyQualifiedClassName.lastIndexOf('.');
        if (i == -1 || i == 0 || i == fullyQualifiedClassName.length() - 1) {
            throw new IllegalArgumentException("String " + fullyQualifiedClassName + " is not a valid fully qualified class name");
        }
        return fullyQualifiedClassName.substring(0, i);
    }

    public static String extractSimpleClassName(ApiDTOModel apiModel) {
        var packageName = extractPackageName(apiModel);
        return apiModel.getClassName().substring(packageName.length() + 1);
    }

    public static String extractPackageName(ApiTypeModel apiModel) {
        return extractPackageName(apiModel.getClassName());
    }

    public static String extractSimpleClassName(ApiTypeModel apiModel) {
        var packageName = extractPackageName(apiModel);
        return apiModel.getClassName().substring(packageName.length() + 1);
    }

    /**
     * Searches for api model definitions in the current maven project.
     *
     * @return a list of resolved api models
     * @throws MojoExecutionException if not api model could be found.
     */
    public List<ApiModel> requireApiModels() throws MojoExecutionException {
        var apiModels = findApiModelFiles()
                .map(this::extract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (apiModels.isEmpty()) {
            throw new MojoExecutionException("No api models found");
        }

        log.debug("Found " + apiModels.size() + " api model(s) in dependencies.");
        return apiModels;
    }

    private Stream<File> findApiModelFiles() {
        if (apiModelJson != null) {
            log.info("Using api modal json file " + apiModelJson.getAbsolutePath());
            return Stream.of(apiModelJson);
        }

        log.debug("Searching api model json in artifacts...");
        return mavenProject.getArtifacts()
                .stream()
                .filter(artifactFilter)
                .map(Artifact::getFile);
    }

    private Optional<ApiModel> extract(File file) {
        if (file.getName().endsWith(".jar")) {
            return new JarApiModelExtractor().extract(file);
        } else if (file.getName().endsWith(".json")) {
            return new ApiModelFileExtractor().extract(file);
        } else {
            log.debug("Cannot extract artifact file " + file.getAbsolutePath());
            return Optional.empty();
        }
    }
}
