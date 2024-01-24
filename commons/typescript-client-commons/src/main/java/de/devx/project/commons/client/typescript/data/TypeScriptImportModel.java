package de.devx.project.commons.client.typescript.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class TypeScriptImportModel {

    private String path;
    private Set<String> identifiers;

}
