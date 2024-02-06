package de.devx.project.commons.processor.spring.data;

import de.devx.project.commons.processor.spring.type.ParameterType;
import lombok.Data;

@Data
public class ParameterAnnotation {

    private String name;
    private boolean required = true;
    private String defaultValue;
    private ParameterType type;
}
