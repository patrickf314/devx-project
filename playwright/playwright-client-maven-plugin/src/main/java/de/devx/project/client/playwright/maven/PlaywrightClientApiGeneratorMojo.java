package de.devx.project.client.playwright.maven;

import de.devx.project.client.playwright.generator.PlaywrightClientGenerator;
import de.devx.project.client.playwright.generator.properties.PlaywrightClientGeneratorProperties;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.api.model.io.ApiModelFileExtractor;
import de.devx.project.commons.api.model.io.JarApiModelExtractor;
import de.devx.project.commons.client.typescript.io.TypeScriptFileGenerator;
import de.devx.project.commons.client.typescript.mapper.TypeScriptDTOMapper;
import de.devx.project.commons.client.typescript.mapper.TypeScriptEnumMapper;
import de.devx.project.commons.client.typescript.mapper.TypeScriptServiceMapper;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.client.typescript.properties.TypeScriptPackageAlias;
import de.devx.project.commons.client.typescript.properties.TypeScriptTypeAlias;
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
        name = "generate-playwright-client",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
public class PlaywrightClientApiGeneratorMojo extends AbstractMojo {

    private static final TypeScriptDTOMapper DTO_MAPPER = Mappers.getMapper(TypeScriptDTOMapper.class);
    private static final TypeScriptEnumMapper ENUM_MAPPER = Mappers.getMapper(TypeScriptEnumMapper.class);
    private static final TypeScriptServiceMapper SERVICE_MAPPER = Mappers.getMapper(TypeScriptServiceMapper.class);

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${project.build.directory}")
    private String outputDirectory;
    @Parameter(property = "apiClientGenerator.apiModelJson")
    private File apiModelJson;

    @Parameter
    private List<TypeScriptTypeAlias> typeAliases;
    @Parameter
    private List<TypeScriptPackageAlias> packageAliases;
    @Parameter
    private String defaultPackageAlias;
    @Parameter
    private TypeScriptDependency httpHeaderCustomizer;
    @Parameter(required = true)
    private TypeScriptDependency testContext;

    @Override
    public void execute() throws MojoExecutionException {
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

        getLog().debug("Searching api model json in artifacts...");
        return mavenProject.getArtifacts()
                .stream()
                .map(Artifact::getFile);
    }

    private void generateClients(List<ApiModel> apiModels) throws IOException {
        var typeAliases = this.typeAliases == null ? Collections.<String, TypeScriptTypeAlias>emptyMap() : this.typeAliases.stream().collect(Collectors.toMap(TypeScriptTypeAlias::getClassName, Function.identity()));
        var fileGenerator = new TypeScriptFileGenerator(outputDirectory);
        var generator = new PlaywrightClientGenerator(fileGenerator, properties());

        for (var apiModel : apiModels) {
            var services = apiModel.getEndpoints().values().stream().map(service -> SERVICE_MAPPER.mapService(service, typeAliases)).toList();
            var dtos = apiModel.getDtos().values().stream()
                    .filter(dto -> !typeAliases.containsKey(dto.getClassName()))
                    .map(dto -> DTO_MAPPER.mapDTO(dto, typeAliases))
                    .toList();
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

            generator.generateUtilities();
            generator.generateServiceInfo(services);
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

    private PlaywrightClientGeneratorProperties properties() {
        return new PlaywrightClientGeneratorProperties(
                typeAliases == null ? Collections.emptyList() : typeAliases,
                packageAliases == null ? Collections.emptyList() : packageAliases,
                defaultPackageAlias,
                httpHeaderCustomizer,
                testContext
        );
    }
}
