package de.devx.project.commons.client.typescript.data;

import lombok.Data;

import java.util.List;

@Data
public class TypeScriptEnumModel {

    private String className;
    private String name;
    private List<String> values;

}
