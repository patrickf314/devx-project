package de.devx.project.commons.client.typescript.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TypeScriptDependency {

    private String identifier;
    private String path;
    private String dependency;

    public TypeScriptDependency(String identifier, String path) {
        this.identifier = identifier;
        this.path = path;
    }

}
