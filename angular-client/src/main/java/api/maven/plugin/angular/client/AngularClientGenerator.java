package api.maven.plugin.angular.client;

import api.maven.plugin.angular.client.data.TypeScriptDependency;
import api.maven.plugin.angular.client.data.TypeScriptTypeAlias;
import api.maven.plugin.core.model.*;
import api.maven.plugin.core.type.ApiMethodResponseType;
import api.maven.plugin.core.type.ApiTypeType;
import api.maven.plugin.angular.client.mapper.TypeScriptMapper;
import api.maven.plugin.angular.client.utils.TypeScriptOutputDirectory;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.mapstruct.factory.Mappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AngularClientGenerator {

    private static final String RXJS_PATH = "rxjs";
    private static final TypeScriptMapper MAPPER = Mappers.getMapper(TypeScriptMapper.class);

    private final TypeScriptOutputDirectory outputDirectory;
    private final Map<String, TypeScriptTypeAlias> typeAliases;
    private final Configuration configuration;

    private final Set<String> copiedResourceFiles = new HashSet<>();

    public AngularClientGenerator(String outputDirectory, List<TypeScriptTypeAlias> typeAliases) throws IOException {
        this.outputDirectory = new TypeScriptOutputDirectory(outputDirectory);
        this.typeAliases = typeAliases.stream().collect(Collectors.toMap(TypeScriptTypeAlias::getClassName, Function.identity()));

        configuration = new Configuration(Configuration.VERSION_2_3_20);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setClassLoaderForTemplateLoading(AngularClientGenerator.class.getClassLoader(), "");
    }

    public void generate(ApiModel model) throws IOException {
        for (var enumModel : model.getEnums().values()) {
            if(typeAliases.containsKey(enumModel.getClassName())) {
                continue;
            }

            generateEnum(enumModel);
        }

        for (var dtoModel : model.getDtos().values()) {
            if(typeAliases.containsKey(dtoModel.getClassName())) {
                continue;
            }

            generateDTO(dtoModel);
        }

        for (var endpointModel : model.getEndpoints().values()) {
            generateEndpoint(endpointModel);
        }
    }

    private void generateEnum(ApiEnumModel enumModel) throws IOException {
        var file = outputDirectory.enumFile(enumModel);
        var template = configuration.getTemplate("enum-template.ts.ftl");

        try (var writer = new FileWriter(file, false)) {
            template.process(Map.of("model", MAPPER.mapEnum(enumModel)), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private void generateDTO(ApiDTOModel dtoModel) throws IOException {
        var file = outputDirectory.dtoFile(dtoModel);
        var template = configuration.getTemplate("dto-template.ts.ftl");

        try (var writer = new FileWriter(file, false)) {
            template.process(Map.of("model", MAPPER.mapDTO(dtoModel, findDependencies(dtoModel), typeAliases)), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private void generateEndpoint(ApiServiceEndpointModel endpointModel) throws IOException {
        var file = outputDirectory.serviceFile(endpointModel);
        var template = configuration.getTemplate("service-template.ts.ftl");

        try (var writer = new FileWriter(file, false)) {
            template.process(Map.of("model", MAPPER.mapService(endpointModel, findDependencies(endpointModel), typeAliases)), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    private Collection<TypeScriptDependency> findDependencies(ApiServiceEndpointModel endpointModel) throws IOException {
        var dependencies = new HashMap<String, TypeScriptDependency>();
        dependencies.put(RXJS_PATH, mapToDependency(RXJS_PATH, Set.of("firstValueFrom")));

        for (var methodModels : endpointModel.getMethods().values()) {
            for (var methodModel : methodModels) {
                addTypeDependencies(endpointModel, methodModel, dependencies);
            }
        }

        return dependencies.values();
    }

    private Collection<TypeScriptDependency> findDependencies(ApiDTOModel dtoModel) {
        var dependencies = new HashMap<String, TypeScriptDependency>();

        if (dtoModel.getExtendedDTO() != null) {
            var alias = typeAliases.get(dtoModel.getExtendedDTO().getClassName());
            if(alias == null) {
                var dependency = mapToDependency(dtoModel, dtoModel.getExtendedDTO());
                dependencies.put(dependency.getPath(), dependency);
            }else if(alias.getTsFile() != null){
                var dependency = mapToDependency("../../../" + alias.getTsFile(), Set.of(alias.getTsType()));
                dependencies.put(dependency.getPath(), dependency);
            }

            for(var typeArgument : dtoModel.getExtendedDTO().getTypeArguments()) {
                addTypeDependencies(dtoModel, typeArgument, dependencies);
            }
        }

        for (var fieldType : dtoModel.getFields().values()) {
            addTypeDependencies(dtoModel, fieldType, dependencies);
        }

        return dependencies.values();
    }

    private void addTypeDependencies(ApiServiceEndpointModel endpointModel, ApiMethodModel methodModel, Map<String, TypeScriptDependency> dependencies) throws IOException {
        if (methodModel.getResponseType() == ApiMethodResponseType.STREAM) {
            ensureFileCopied("download-info.ts");
            var path = outputDirectory.relativePathToCommonsFile(endpointModel, "download-info");
            dependencies.put(path, mapToDependency(path, Set.of("HTTP_DOWNLOAD_OPTIONS", "HTTP_DOWNLOAD_PIPE", "DownloadInfo")));
            dependencies.get(RXJS_PATH).getIdentifiers().add("Observable");
        } else if (methodModel.getResponseType() == ApiMethodResponseType.SERVER_SEND_EVENT) {
            ensureFileCopied("server-send-event.ts");
            var path = outputDirectory.relativePathToCommonsFile(endpointModel, "server-send-event");
            dependencies.put(path, mapToDependency(path, Set.of("ServerSendEventSource", "ServerSentEvent")));
        } else {
            addTypeDependencies(endpointModel, methodModel.getReturnType(), dependencies);
        }

        methodModel.getParameters().forEach(param -> addTypeDependencies(endpointModel, param.getType(), dependencies));
    }

    private void ensureFileCopied(String fileName) throws IOException {
        if (copiedResourceFiles.contains(fileName)) {
            return;
        }

        var fileUrl = getClass().getClassLoader().getResource(fileName);
        if (fileUrl == null) {
            throw new FileNotFoundException("Resource file " + fileName + " not found");
        }

        var folder = outputDirectory.commonsFile(fileName);

        try (var in = fileUrl.openStream()) {
            Files.copy(in, folder.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        copiedResourceFiles.add(fileName);
    }

    private void addTypeDependencies(ApiServiceEndpointModel endpointModel, ApiTypeModel returnOrParamType, Map<String, TypeScriptDependency> dependencies) {
        var alias = typeAliases.get(returnOrParamType.getClassName());
        if(alias == null) {
            switch (returnOrParamType.getType()) {
                case JAVA_TYPE -> addJavaTypeDependencies(endpointModel, returnOrParamType, dependencies);
                case ENUM, DTO -> addEnumOrDTOTypeDependencies(endpointModel, returnOrParamType, dependencies);
            }

            return;
        }

        if(alias.getTsFile() != null) {
            dependencies.computeIfAbsent("../../" + alias.getTsFile(), path -> mapToDependency(path, new HashSet<>())).getIdentifiers().add(alias.getTsType());
        }
    }

    private void addTypeDependencies(ApiDTOModel dtoModel, ApiTypeModel fieldType, Map<String, TypeScriptDependency> dependencies) {
        var alias = typeAliases.get(fieldType.getClassName());
        if(alias == null) {
            switch (fieldType.getType()) {
                case JAVA_TYPE -> addJavaTypeDependencies(dtoModel, fieldType, dependencies);
                case ENUM, DTO -> addEnumOrDTOTypeDependencies(dtoModel, fieldType, dependencies);
            }

            return;
        }

        if(alias.getTsFile() != null) {
            dependencies.computeIfAbsent("../../../" + alias.getTsFile(), path -> mapToDependency(path, new HashSet<>())).getIdentifiers().add(alias.getTsType());
        }
    }

    private void addEnumOrDTOTypeDependencies(ApiDTOModel dtoModel, ApiTypeModel fieldType, Map<String, TypeScriptDependency> dependencies) {
        fieldType.getTypeArguments().forEach(arg -> addTypeDependencies(dtoModel, arg, dependencies));

        if (fieldType.getType() == ApiTypeType.DTO && dtoModel.getClassName().equals(fieldType.getClassName())) {
            return;
        }

        var path = outputDirectory.relativePath(dtoModel, fieldType);
        addTypeDependency(fieldType, dependencies, path);
    }

    private void addEnumOrDTOTypeDependencies(ApiServiceEndpointModel endpointModel, ApiTypeModel returnOrParamType, Map<String, TypeScriptDependency> dependencies) {
        returnOrParamType.getTypeArguments().forEach(arg -> addTypeDependencies(endpointModel, arg, dependencies));

        var path = outputDirectory.relativePath(endpointModel, returnOrParamType);
        addTypeDependency(returnOrParamType, dependencies, path);
    }

    private void addJavaTypeDependencies(ApiDTOModel dtoModel, ApiTypeModel fieldType, Map<String, TypeScriptDependency> dependencies) {
        switch (fieldType.getName()) {
            case "collection", "array" ->
                    addTypeDependencies(dtoModel, fieldType.getTypeArguments().get(0), dependencies);
            case "map" -> addTypeDependencies(dtoModel, fieldType.getTypeArguments().get(1), dependencies);
        }
    }

    private void addJavaTypeDependencies(ApiServiceEndpointModel endpointModel, ApiTypeModel returnOrParamType, Map<String, TypeScriptDependency> dependencies) {
        switch (returnOrParamType.getName()) {
            case "collection", "array" ->
                    addTypeDependencies(endpointModel, returnOrParamType.getTypeArguments().get(0), dependencies);
            case "map" -> addTypeDependencies(endpointModel, returnOrParamType.getTypeArguments().get(1), dependencies);
        }
    }

    private void addTypeDependency(ApiTypeModel typeModel, Map<String, TypeScriptDependency> dependencies, String path) {
        var dependency = dependencies.get(path);
        if (dependency == null) {
            dependencies.put(path, mapToDependency(path, Set.of(typeModel.getName())));
        } else {
            dependency.getIdentifiers().add(typeModel.getName());
        }
    }

    private TypeScriptDependency mapToDependency(ApiDTOModel dtoModel, ApiTypeModel typeModel) {
        return mapToDependency(outputDirectory.relativePath(dtoModel, typeModel), Set.of(typeModel.getName()));
    }

    private TypeScriptDependency mapToDependency(String path, Set<String> identifiers) {
        var dependency = new TypeScriptDependency();
        dependency.setPath(path);
        dependency.setIdentifiers(new HashSet<>(identifiers));
        return dependency;
    }
}
