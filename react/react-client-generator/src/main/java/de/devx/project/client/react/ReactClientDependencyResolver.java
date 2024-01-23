package de.devx.project.client.react;

import de.devx.project.commons.api.model.data.ApiServiceEndpointModel;
import de.devx.project.commons.api.model.type.ApiMethodResponseType;
import de.devx.project.commons.client.typescript.TypeScriptDependencyResolver;
import de.devx.project.commons.client.typescript.data.TypeScriptDependencyModel;
import de.devx.project.commons.client.typescript.io.TypeScriptRelativePaths;
import de.devx.project.commons.client.typescript.io.TypeScriptTypeAlias;

import java.util.*;
import java.util.stream.Collectors;

public class ReactClientDependencyResolver extends TypeScriptDependencyResolver {

    public ReactClientDependencyResolver(TypeScriptRelativePaths relativePaths, Map<String, TypeScriptTypeAlias> typeAliases) {
        super(relativePaths, typeAliases);
    }

    public Collection<TypeScriptDependencyModel> resolveDependencies(ApiServiceEndpointModel endpointModel) {
        var dependencies = new HashMap<>(Map.of(
                "@reduxjs/toolkit", Set.of("createAsyncThunk"),
                "../../slice/app-store", Set.of("AppState", "useAppDispatch"),
                "react", Set.of("useMemo"),
                "../../common/service/thunk-config", Set.of("ThunkOptions")
        ));

        var returnTypes = endpointModel.getMethods().values().stream().flatMap(List::stream).map(method -> {
            if (method.getResponseType() == ApiMethodResponseType.STREAM) {
                return "stream";
            }

            if ("void".equals(method.getReturnType().getName())) {
                return "void";
            } else {
                return "json";
            }
        }).collect(Collectors.toSet());

        var serviceCommonsDependencies = new HashSet<>(Set.of("backendUrl", "prepareHeaders", "url"));
        if (returnTypes.contains("void")) {
            serviceCommonsDependencies.add("mapVoidResponse");
        }
        if (returnTypes.contains("stream")) {
            serviceCommonsDependencies.add("mapStreamingResponse");
            dependencies.put("../../common/service/download-stream.dto", Set.of("DownloadStreamDTO"));
        }
        if (returnTypes.contains("json")) {
            serviceCommonsDependencies.add("mapJsonResponse");
        }

        dependencies.put("../../common/service/service-commons", serviceCommonsDependencies);

        return this.resolveDependencies(endpointModel, dependencies);
    }
}
