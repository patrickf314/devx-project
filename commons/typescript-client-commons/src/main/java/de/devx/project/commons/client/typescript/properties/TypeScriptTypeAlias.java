package de.devx.project.commons.client.typescript.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TypeScriptTypeAlias {
    private String className;
    private String type;
    private String dependency;
    private String path;
    private String zodSchema;
    private String zodSchemaDependency;
    private String zodSchemaPath;
}
