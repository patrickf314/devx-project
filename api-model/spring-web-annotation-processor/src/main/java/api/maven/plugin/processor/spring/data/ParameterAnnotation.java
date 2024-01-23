package api.maven.plugin.processor.spring.data;

import de.devx.project.commons.api.model.type.ApiMethodParameterType;

public class ParameterAnnotation {

    private String name;
    private boolean required = true;
    private String defaultValue;
    private ApiMethodParameterType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ApiMethodParameterType getType() {
        return type;
    }

    public void setType(ApiMethodParameterType type) {
        this.type = type;
    }
}
