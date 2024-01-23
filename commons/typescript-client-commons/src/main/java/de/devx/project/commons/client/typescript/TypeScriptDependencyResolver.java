package de.devx.project.commons.client.typescript;

import de.devx.project.commons.api.model.data.ApiDTOModel;
import de.devx.project.commons.api.model.data.ApiMethodModel;
import de.devx.project.commons.api.model.data.ApiServiceEndpointModel;
import de.devx.project.commons.api.model.data.ApiTypeModel;
import de.devx.project.commons.api.model.type.ApiTypeType;
import de.devx.project.commons.client.typescript.data.TypeScriptDependencyModel;
import de.devx.project.commons.client.typescript.io.TypeScriptRelativePaths;
import de.devx.project.commons.client.typescript.io.TypeScriptTypeAlias;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class TypeScriptDependencyResolver {

    private final TypeScriptRelativePaths relativePaths;
    private final Map<String, TypeScriptTypeAlias> typeAliases;

    protected Collection<TypeScriptDependencyModel> resolveDependencies(ApiServiceEndpointModel endpointModel, Map<String, Set<String>> additionalDependencies) {
        var dependencies = new HashMap<String, TypeScriptDependencyModel>();
        for (var entry : additionalDependencies.entrySet()) {
            dependencies.put(entry.getKey(), mapToDependency(entry.getKey(), entry.getValue()));
        }

        for (var methodModels : endpointModel.getMethods().values()) {
            for (var methodModel : methodModels) {
                addTypeDependencies(endpointModel, methodModel, dependencies);
            }
        }

        return dependencies.values();
    }

    public Collection<TypeScriptDependencyModel> resolveDependencies(ApiDTOModel dtoModel) {
        var dependencies = new HashMap<String, TypeScriptDependencyModel>();

        if (dtoModel.getExtendedDTO() != null) {
            var alias = typeAliases.get(dtoModel.getExtendedDTO().getClassName());
            if (alias == null) {
                var dependency = mapToDependency(dtoModel, dtoModel.getExtendedDTO());
                dependencies.put(dependency.getPath(), dependency);
            } else if (alias.getTsPath() != null) {
                var dependency = mapToDependency(alias.getTsPath(), Set.of(alias.getTsType()));
                dependencies.put(dependency.getPath(), dependency);
            } else if (alias.getTsFile() != null) {
                var dependency = mapToDependency("../../../" + alias.getTsFile(), Set.of(alias.getTsType()));
                dependencies.put(dependency.getPath(), dependency);
            }

            for (var typeArgument : dtoModel.getExtendedDTO().getTypeArguments()) {
                addTypeDependencies(dtoModel, typeArgument, dependencies);
            }
        }

        for (var fieldType : dtoModel.getFields().values()) {
            addTypeDependencies(dtoModel, fieldType, dependencies);
        }

        return dependencies.values();
    }

    protected void addTypeDependencies(ApiServiceEndpointModel endpointModel, ApiMethodModel methodModel, Map<String, TypeScriptDependencyModel> dependencies) {
        addTypeDependencies(endpointModel, methodModel.getReturnType(), dependencies);
        methodModel.getParameters().forEach(param -> addTypeDependencies(endpointModel, param.getType(), dependencies));
    }

    private void addTypeDependencies(ApiServiceEndpointModel endpointModel, ApiTypeModel returnOrParamType, Map<String, TypeScriptDependencyModel> dependencies) {
        var alias = typeAliases.get(returnOrParamType.getClassName());
        if (alias == null || (alias.getAnnotation() != null && !returnOrParamType.getAnnotations().contains(alias.getAnnotation()))) {
            switch (returnOrParamType.getType()) {
                case JAVA_TYPE -> addJavaTypeDependencies(endpointModel, returnOrParamType, dependencies);
                case ENUM, DTO -> addEnumOrDTOTypeDependencies(endpointModel, returnOrParamType, dependencies);
            }

            return;
        }

        if (alias.getTsPath() != null) {
            dependencies.computeIfAbsent(alias.getTsPath(), path -> mapToDependency(path, new HashSet<>())).getIdentifiers().add(alias.getTsType());
        } else if (alias.getTsFile() != null) {
            dependencies.computeIfAbsent("../../" + alias.getTsFile(), path -> mapToDependency(path, new HashSet<>())).getIdentifiers().add(alias.getTsType());
        }
    }

    private void addTypeDependencies(ApiDTOModel dtoModel, ApiTypeModel fieldType, Map<String, TypeScriptDependencyModel> dependencies) {
        var alias = typeAliases.get(fieldType.getClassName());
        if (alias == null || (alias.getAnnotation() != null && !fieldType.getAnnotations().contains(alias.getAnnotation()))) {
            switch (fieldType.getType()) {
                case JAVA_TYPE -> addJavaTypeDependencies(dtoModel, fieldType, dependencies);
                case ENUM, DTO -> addEnumOrDTOTypeDependencies(dtoModel, fieldType, dependencies);
            }

            return;
        }

        if (alias.getTsPath() != null) {
            dependencies.computeIfAbsent(alias.getTsPath(), path -> mapToDependency(path, new HashSet<>())).getIdentifiers().add(alias.getTsType());
        } else if (alias.getTsFile() != null) {
            dependencies.computeIfAbsent("../../../" + alias.getTsFile(), path -> mapToDependency(path, new HashSet<>())).getIdentifiers().add(alias.getTsType());
        }
    }

    private void addEnumOrDTOTypeDependencies(ApiDTOModel dtoModel, ApiTypeModel fieldType, Map<String, TypeScriptDependencyModel> dependencies) {
        fieldType.getTypeArguments().forEach(arg -> addTypeDependencies(dtoModel, arg, dependencies));

        if (fieldType.getType() == ApiTypeType.DTO && dtoModel.getClassName().equals(fieldType.getClassName())) {
            return;
        }

        var path = relativePaths.get(dtoModel, fieldType);
        addTypeDependency(fieldType, dependencies, path);
    }

    private void addEnumOrDTOTypeDependencies(ApiServiceEndpointModel endpointModel, ApiTypeModel returnOrParamType, Map<String, TypeScriptDependencyModel> dependencies) {
        returnOrParamType.getTypeArguments().forEach(arg -> addTypeDependencies(endpointModel, arg, dependencies));

        var path = relativePaths.get(endpointModel, returnOrParamType);
        addTypeDependency(returnOrParamType, dependencies, path);
    }

    private void addJavaTypeDependencies(ApiDTOModel dtoModel, ApiTypeModel fieldType, Map<String, TypeScriptDependencyModel> dependencies) {
        switch (fieldType.getName()) {
            case "collection", "array" ->
                    addTypeDependencies(dtoModel, fieldType.getTypeArguments().get(0), dependencies);
            case "map" -> addTypeDependencies(dtoModel, fieldType.getTypeArguments().get(1), dependencies);
        }
    }

    private void addJavaTypeDependencies(ApiServiceEndpointModel endpointModel, ApiTypeModel returnOrParamType, Map<String, TypeScriptDependencyModel> dependencies) {
        switch (returnOrParamType.getName()) {
            case "collection", "array" ->
                    addTypeDependencies(endpointModel, returnOrParamType.getTypeArguments().get(0), dependencies);
            case "map" -> addTypeDependencies(endpointModel, returnOrParamType.getTypeArguments().get(1), dependencies);
        }
    }

    private void addTypeDependency(ApiTypeModel typeModel, Map<String, TypeScriptDependencyModel> dependencies, String path) {
        var dependency = dependencies.get(path);
        if (dependency == null) {
            dependencies.put(path, mapToDependency(path, Set.of(typeModel.getName())));
        } else {
            dependency.getIdentifiers().add(typeModel.getName());
        }
    }

    private TypeScriptDependencyModel mapToDependency(ApiDTOModel dtoModel, ApiTypeModel typeModel) {
        return mapToDependency(relativePaths.get(dtoModel, typeModel), Set.of(typeModel.getName()));
    }

    private TypeScriptDependencyModel mapToDependency(String path, Set<String> identifiers) {
        var dependency = new TypeScriptDependencyModel();
        dependency.setPath(path);
        dependency.setIdentifiers(new HashSet<>(identifiers));
        return dependency;
    }
}
