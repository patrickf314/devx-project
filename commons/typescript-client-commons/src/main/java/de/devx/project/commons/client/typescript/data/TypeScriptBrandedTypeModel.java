package de.devx.project.commons.client.typescript.data;

import lombok.Data;

@Data
public class TypeScriptBrandedTypeModel {

    private String className;
    private String name;
    private String underlyingTsType;
    private String underlyingZodSchema;

}
