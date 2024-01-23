package api.maven.plugin.react.client;

import de.devx.project.client.react.ReactClientDependencyResolver;
import de.devx.project.client.react.ReactClientGenerator;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.io.ApiModelFileExtractor;
import de.devx.project.commons.api.model.io.JarApiModelExtractor;
import de.devx.project.commons.client.typescript.io.TypeScriptFileGenerator;
import de.devx.project.commons.client.typescript.io.TypeScriptPackageAlias;
import de.devx.project.commons.client.typescript.io.TypeScriptRelativePaths;
import de.devx.project.commons.client.typescript.io.TypeScriptTypeAlias;
import de.devx.project.commons.client.typescript.mapper.TypeScriptDTOMapper;
import de.devx.project.commons.client.typescript.mapper.TypeScriptEnumMapper;
import de.devx.project.commons.client.typescript.mapper.TypeScriptServiceMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(
        name = "generate-react-client",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
public class ReactClientApiGeneratorMojo extends AbstractMojo {

    private static final TypeScriptDTOMapper DTO_MAPPER = Mappers.getMapper(TypeScriptDTOMapper.class);
    private static final TypeScriptEnumMapper ENUM_MAPPER = Mappers.getMapper(TypeScriptEnumMapper.class);
    private static final TypeScriptServiceMapper SERVICE_MAPPER = Mappers.getMapper(TypeScriptServiceMapper.class);

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject mavenProject;
    @Parameter(defaultValue = "${project.build.directory}")
    private String outputDirectory;
    @Parameter
    private List<TypeScriptTypeAlias> typeScriptTypeAliases;
    @Parameter(property = "apiClientGenerator.apiModelJson")
    private File apiModelJson;
    @Parameter
    private String defaultPackage;
    @Parameter
    private List<TypeScriptPackageAlias> typeScriptPackageAliases;

    @Override
    public void execute() throws MojoExecutionException {
        if (typeScriptTypeAliases == null) {
            typeScriptTypeAliases = Collections.emptyList();
        }

        if (typeScriptPackageAliases == null) {
            typeScriptPackageAliases = Collections.emptyList();
        }

        getLog().info("Starting generation of API clients...");
        getLog().debug("Searing for api model definition in dependencies...");

        var apiModels = findApiModelFiles()
                .map(this::extract)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (apiModels.isEmpty()) {
            throw new MojoExecutionException("No api models found");
        }

        getLog().debug("Found " + apiModels.size() + " api model(s) in dependencies, start client generations");

        try {
            generateClients(apiModels);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate client code", e);
        }

        getLog().info("API client generation completed");
    }

    private Stream<File> findApiModelFiles() {
        if (apiModelJson != null) {
            getLog().info("Using api modal json file " + apiModelJson.getAbsolutePath());
            return Stream.of(apiModelJson);
        }

        getLog().info("Searching api modal json in artifacts...");
        return mavenProject.getArtifacts()
                .stream()
                .map(Artifact::getFile);
    }

    private void generateClients(List<ApiModel> apiModels) throws IOException {
        var typeAliases = typeScriptTypeAliases.stream().collect(Collectors.toMap(TypeScriptTypeAlias::getClassName, Function.identity()));
        var fileGenerator = new TypeScriptFileGenerator(outputDirectory);
        var generator = new ReactClientGenerator(fileGenerator, typeScriptPackageAliases, defaultPackage);
        var relativePaths = new TypeScriptRelativePaths(fileGenerator, generator::extractPackageNameFromClassName);
        var dependencyResolver = new ReactClientDependencyResolver(relativePaths, typeAliases);

        for (var apiModel : apiModels) {
            var services = apiModel.getEndpoints().values().stream().map(service -> SERVICE_MAPPER.mapService(service, dependencyResolver.resolveDependencies(service), typeAliases)).toList();
            var dtos = apiModel.getDtos().values().stream().map(dto -> DTO_MAPPER.mapDTO(dto, dependencyResolver.resolveDependencies(dto), typeAliases)).toList();
            var enums = apiModel.getEnums().values().stream().map(ENUM_MAPPER::mapEnum).toList();

            for (var service : services) {
                generator.generateService(service);
            }

            for (var dto : dtos) {
                generator.generateDTO(dto);
            }

            for (var type : enums) {
                generator.generateEnum(type);
            }
        }
    }

    private Optional<ApiModel> extract(File file) {
        if (file.getName().endsWith(".jar")) {
            return new JarApiModelExtractor().extract(file);
        } else if (file.getName().endsWith(".json")) {
            return new ApiModelFileExtractor().extract(file);
        } else {
            getLog().debug("Cannot extract artifact file " + file.getAbsolutePath());
            return Optional.empty();
        }
    }
}
