package de.devx.project.client.typescript.maven;

import de.devx.project.commons.api.model.data.ApiModel;
import de.devx.project.commons.client.typescript.TypescriptClientGenerator;
import de.devx.project.commons.client.typescript.io.TypeScriptFileGenerator;
import de.devx.project.commons.client.typescript.mapper.TypeScriptDTOMapper;
import de.devx.project.commons.client.typescript.mapper.TypeScriptEnumMapper;
import de.devx.project.commons.client.typescript.properties.TypeScriptClientGeneratorProperties;
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
        name = "generate-dto",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        threadSafe = true
)
public class TypeScriptDtoGeneratorMojo extends AbstractMojo {

    private static final TypeScriptDTOMapper DTO_MAPPER = Mappers.getMapper(TypeScriptDTOMapper.class);
    private static final TypeScriptEnumMapper ENUM_MAPPER = Mappers.getMapper(TypeScriptEnumMapper.class);

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

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Starting generation of typescript DTO interfaces...");
        getLog().debug("Searing for api model definition in dependencies...");

        var apiModelResolver = new MavenApiModelParser(getLog(), apiModelJson, mavenProject);
        var apiModels = apiModelResolver.requireApiModels();

        try {
            generateDTOs(apiModels);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate typescript DTO code", e);
        }

        getLog().info("Typescript DTO generation completed");
    }

    private void generateDTOs(List<ApiModel> apiModels) throws IOException {
        var typeAliases = this.typeAliases == null ? Collections.<String, TypeScriptTypeAlias>emptyMap() : this.typeAliases.stream().collect(Collectors.toMap(TypeScriptTypeAlias::getClassName, Function.identity()));
        var fileGenerator = new TypeScriptFileGenerator(outputDirectory);
        var generator = new TypescriptClientGenerator<>(fileGenerator, properties());

        for (var apiModel : apiModels) {
            var dtos = apiModel.getDtos().values().stream()
                    .filter(dto -> !typeAliases.containsKey(dto.getClassName()))
                    .map(dto -> DTO_MAPPER.mapDTO(dto, typeAliases))
                    .toList();
            var enums = apiModel.getEnums().values().stream().map(ENUM_MAPPER::mapEnum).toList();

            for (var dto : dtos) {
                generator.generateDTO(dto);
            }

            for (var type : enums) {
                generator.generateEnum(type);
            }
        }
    }

    private TypeScriptClientGeneratorProperties properties() {
        return new TypeScriptClientGeneratorProperties(typeAliases, packageAliases, defaultPackageAlias);
    }
}
