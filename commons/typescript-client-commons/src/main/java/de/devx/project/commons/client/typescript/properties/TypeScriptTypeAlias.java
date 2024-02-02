package de.devx.project.commons.client.typescript.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeScriptTypeAlias {
    private String className;
    private String type;
    private String path;
}
