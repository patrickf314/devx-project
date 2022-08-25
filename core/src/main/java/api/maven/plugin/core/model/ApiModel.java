package api.maven.plugin.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ApiModel {

    private final Map<String, ApiServiceEndpointModel> endpoints = new HashMap<>();
    private final Map<String, ApiDTOModel> dtos = new HashMap<>();
    private final Map<String, ApiEnumModel> enums = new HashMap<>();

    public void addEndpoint(ApiServiceEndpointModel model) {
        this.endpoints.put(model.getClassName(), model);
    }

    public void addDTO(ApiDTOModel model) {
        this.dtos.put(model.getClassName(), model);
    }

    public void addEnum(ApiEnumModel model) {
        this.enums.put(model.getClassName(), model);
    }
}
