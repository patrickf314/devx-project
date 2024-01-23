package de.devx.project.commons.client.typescript.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TypeScriptDTOFieldModel {

    private String name;
    private TypeScriptTypeModel type;

}
