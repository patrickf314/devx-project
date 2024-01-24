package de.devx.project.commons.client.typescript.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class TypeScriptServiceModel {

    private String className;
    private String name;

    private List<TypeScriptPathModel> basePaths = new ArrayList<>();
    private List<TypeScriptServiceMethodModel> methods = new ArrayList<>();

}
