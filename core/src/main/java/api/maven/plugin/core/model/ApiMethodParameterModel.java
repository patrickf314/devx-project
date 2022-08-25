package api.maven.plugin.core.model;

import api.maven.plugin.core.type.ApiMethodParameterType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiMethodParameterModel {

    private String name;

    private ApiTypeModel type;
    private ApiMethodParameterType in;
    private String defaultValue;
    private String parameterName;

    public ApiMethodParameterModel(String name) {
        this.name = name;
    }
}
