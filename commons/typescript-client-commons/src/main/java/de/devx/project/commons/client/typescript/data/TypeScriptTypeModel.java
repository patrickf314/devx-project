package de.devx.project.commons.client.typescript.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class TypeScriptTypeModel {

    private String name;
    private boolean optional;

    private Set<String> dependentClassNames;
}
