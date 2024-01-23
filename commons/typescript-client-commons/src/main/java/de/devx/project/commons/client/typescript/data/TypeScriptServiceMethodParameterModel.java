package de.devx.project.commons.client.typescript.data;

import lombok.Data;

@Data
public class TypeScriptServiceMethodParameterModel {

    private String name;
    private String parameterName;
    private TypeScriptTypeModel type;
    private String defaultValue;

    public boolean isOptional() {
        return defaultValue != null || type.isOptional();
    }
}

