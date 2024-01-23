package api.maven.plugin.angular.client.data;

import lombok.Data;

@Data
public class TypeScriptServiceMethodParameter {

    private String name;
    private String parameterName;
    private TypeScriptType type;
    private String defaultValue;

    public boolean isOptional() {
        return defaultValue != null || type.isOptional();
    }
}

