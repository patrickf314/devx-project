package api.maven.plugin.angular.client.utils;

import de.devx.project.commons.api.model.data.ApiMethodModel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TypeScriptMethodNameUtils {

    private TypeScriptMethodNameUtils() {
        // No instances
    }

    public static TypeScriptMethodNameTable createNameTable(List<ApiMethodModel> models) {
        var pathsByHttpMethod = new HashMap<String, List<String>>();
        for (var model : models) {
            for (var httpMethod : model.getHttpMethods()) {
                var paths = model.getPaths();
                if(paths.isEmpty()) {
                    paths = List.of("");
                }

                pathsByHttpMethod.computeIfAbsent(httpMethod, x -> new ArrayList<>()).addAll(paths);
            }
        }

        return new TypeScriptMethodNameTable(pathsByHttpMethod);
    }

    @RequiredArgsConstructor
    public static class TypeScriptMethodNameTable {

        private final Map<String, List<String>> pathsByHttpMethod;

        public String lookupNameFor(String baseName, String httpMethod, String path) {
            var builder = new StringBuilder(baseName);
            if(pathsByHttpMethod.size() > 1) {
                builder.append("Using").append(httpMethod);
            }

            var paths = pathsByHttpMethod.get(httpMethod);
            if(paths == null) {
                throw new IllegalArgumentException("Unknown http method " + httpMethod + " for method " + baseName);
            }

            if(paths.size() > 1) {
                var i = paths.indexOf(path);
                if(i == -1) {
                    throw new IllegalArgumentException("Unknown path " + path + " for method " + baseName);
                }

                builder.append("$").append(paths.indexOf(path) + 1);
            }

            return builder.toString();
        }
    }
}
