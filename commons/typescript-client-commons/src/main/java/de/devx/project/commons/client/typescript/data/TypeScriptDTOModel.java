package de.devx.project.commons.client.typescript.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TypeScriptDTOModel {

    private String className;
    private String name;
    private List<String> typeArguments = new ArrayList<>();
    private TypeScriptTypeModel extendedDTO;
    private List<TypeScriptDTOFieldModel> fields = new ArrayList<>();

}
