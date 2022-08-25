package api.maven.plugin.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ApiServiceEndpointModel {

    private String className;
    private String name;

    private List<String> basePaths = new ArrayList<>();
    private Map<String, List<ApiMethodModel>> methods = new HashMap<>();

    public ApiServiceEndpointModel(String className, String name) {
        this.className = className;
        this.name = name;
    }

    public void addMethod(ApiMethodModel method) {
        this.methods.computeIfAbsent(method.getName(), s -> new ArrayList<>()).add(method);
    }

}
