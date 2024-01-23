package de.devx.project.commons.client.typescript.data;

import lombok.Data;

import java.util.Set;

@Data
public class TypeScriptDependencyModel {

    private String path;
    private Set<String> identifiers;

}
