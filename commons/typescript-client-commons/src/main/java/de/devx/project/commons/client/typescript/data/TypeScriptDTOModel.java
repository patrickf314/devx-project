package de.devx.project.commons.client.typescript.data;

import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class TypeScriptDTOModel {

    private Collection<TypeScriptDependencyModel> dependencies;
    private String className;
    private String name;
    private List<String> typeArguments;
    private TypeScriptTypeModel extendedDTO;
    private List<TypeScriptDTOFieldModel> fields;

}
