package de.devx.project.client.react.maven;

import de.devx.project.client.react.ReactClientGenerator;
import de.devx.project.client.react.properties.ReactClientGeneratorProperties;
import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.client.typescript.io.TypeScriptFileGenerator;
import de.devx.project.commons.client.typescript.mapper.TypeScriptDTOMapper;
import de.devx.project.commons.client.typescript.mapper.TypeScriptEnumMapper;
import de.devx.project.commons.client.typescript.mapper.TypeScriptServiceMapper;
import de.devx.project.commons.client.typescript.properties.TypeScriptDependency;
import de.devx.project.commons.client.typescript.properties.TypeScriptPackageAlias;
import de.devx.project.commons.client.typescript.properties.TypeScriptTypeAlias;
import de.devx.project.commons.maven.parser.MavenApiModelParser;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Parameter(property = "apiClientGenerator.apiModelJson")
    private File apiModelJson;

    @Parameter
    private List<TypeScriptTypeAlias> typeAliases;
    @Parameter
    private List<TypeScriptPackageAlias> packageAliases;
    @Parameter
    private String defaultPackageAlias;
    @Parameter(required = true)
    private TypeScriptDependency errorMapper;
    @Parameter(required = true)
    private TypeScriptDependency errorSerializer;
    @Parameter(required = true)
    private TypeScriptDependency reduxThunkConfig;
    @Parameter
    private TypeScriptDependency httpHeaderCustomizer;
    @Parameter
    private TypeScriptDependency backendUrlGetter;
    @Parameter
    private String backendUrl;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting generation of API clients...");
        getLog().debug("Searing for api model definition in dependencies...");

        var apiModelResolver = new MavenApiModelParser(getLog(), apiModelJson, mavenProject, _ -> true);
        var apiModels = apiModelResolver.requireApiModels();

        getLog().debug("Start client generation...");
        generateClients(apiModels);
        mavenProject.addCompileSourceRoot(outputDirectory);
        getLog().info("API client generation completed");
    }

    private void generateClients(List<ApiModel> apiModels) throws MojoExecutionException {
        var typeAliasMap = this.typeAliases == null ? Collections.<String, TypeScriptTypeAlias>emptyMap() : this.typeAliases.stream().collect(Collectors.toMap(TypeScriptTypeAlias::getClassName, Function.identity()));
        var fileGenerator = new TypeScriptFileGenerator(outputDirectory);
        var generator = new ReactClientGenerator(fileGenerator, properties());

        for (var apiModel : apiModels) {
            var services = apiModel.getEndpoints().values().stream().map(service -> SERVICE_MAPPER.mapService(service, typeAliasMap)).toList();
            var dtos = apiModel.getDtos().values().stream()
                    .filter(dto -> !typeAliasMap.containsKey(dto.getClassName()))
                    .map(dto -> DTO_MAPPER.mapDTO(dto, typeAliasMap))
                    .toList();
            var enums = apiModel.getEnums().values().stream().map(ENUM_MAPPER::mapEnum).toList();

            for (var service : services) {
                generateFileFor(service.getClassName(), () -> generator.generateService(service));
            }

            for (var dto : dtos) {
                generateFileFor(dto.getClassName(), () -> generator.generateDTO(dto));
            }

            for (var type : enums) {
                generateFileFor(type.getClassName(), () -> generator.generateEnum(type));
            }

            generateFileFor("utilities", generator::generateUtilities);
            getLog().debug("Generated " + services.size() + " services, " + dtos.size() + " DTOs and " + enums.size() + " enums in " + outputDirectory);
        }
    }

    private void generateFileFor(String className, FileGenerator generator) throws MojoExecutionException {
        try {
            generator.generateFile();
        } catch (IOException | RuntimeException e) {
            throw new MojoExecutionException("Failed to generate client code for " + className, e);
        }
    }

    private ReactClientGeneratorProperties properties() {
        return new ReactClientGeneratorProperties(
                typeAliases == null ? Collections.emptyList() : typeAliases,
                packageAliases == null ? Collections.emptyList() : packageAliases,
                defaultPackageAlias,
                errorMapper,
                errorSerializer,
                reduxThunkConfig,
                httpHeaderCustomizer,
                backendUrlGetter,
                backendUrl
        );
    }

    interface FileGenerator {

        void generateFile() throws IOException;

    }
}
