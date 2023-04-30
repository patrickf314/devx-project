package api.maven.plugin.angular.client;

import api.maven.plugin.angular.client.data.TypeScriptDependency;
import api.maven.plugin.angular.client.data.TypeScriptTypeAlias;
import api.maven.plugin.angular.client.utils.TypeScriptOutputDirectory;
import api.maven.plugin.core.model.ApiServiceEndpointModel;
import api.maven.plugin.core.type.ApiMethodResponseType;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ReactClientGenerator extends TypeScriptClientGenerator {

    public ReactClientGenerator(String outputDirectory, List<TypeScriptTypeAlias> typeAliases) throws IOException {
        super(new TypeScriptOutputDirectory(outputDirectory) {
            @Override
            protected String getServiceByClassName(String className) {
                if (!className.startsWith("de.badminton.neubiberg.backend.api.")) {
                    return "common";
                }

                var i = "de.badminton.neubiberg.backend.api.".length();
                var j = className.indexOf('.', i);
                if (j == -1) {
                    return "common";
                }

                return className.substring(i, j);
            }
        }, typeAliases);
    }

    @Override
    protected void generateEndpoint(ApiServiceEndpointModel endpointModel) throws IOException {
        var file = outputDirectory.serviceFile(endpointModel);
        var template = configuration.getTemplate("react-service-template.ts.ftl");

        try (var writer = new FileWriter(file, false)) {
            template.process(Map.of("model", MAPPER.mapService(endpointModel, findDependencies(endpointModel), typeAliases)), writer);
        } catch (TemplateException e) {
            throw new IOException("Failed to process template", e);
        }
    }

    protected Collection<TypeScriptDependency> findDependencies(ApiServiceEndpointModel endpointModel) throws IOException {
        var dependencies = new HashMap<>(Map.of(
                "@reduxjs/toolkit", Set.of("createAsyncThunk"),
                "../../slice/app-store", Set.of("AppState", "useAppDispatch"),
                "react", Set.of("useMemo")
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

        var serviceCommonsDependencies = new HashSet<>(Set.of("backendUrl", "prepareHeaders", "ThunkOptions", "url"));
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

        return

                findDependencies(endpointModel, dependencies);
    }
}
