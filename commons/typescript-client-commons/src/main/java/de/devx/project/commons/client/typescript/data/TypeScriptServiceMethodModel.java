package de.devx.project.commons.client.typescript.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TypeScriptServiceMethodModel {

    private String name;
    private TypeScriptTypeModel returnType;
    private List<TypeScriptServiceMethodParameterModel> parameters = new ArrayList<>();
    private String httpMethod;
    private TypeScriptPathModel path;
    private String returnTypeWrapper;
    private String returnTypeMapper;
    private String options;
    private boolean formData;
    private List<TypeScriptServiceMethodParameterModel> headerParams = new ArrayList<>();
    private List<TypeScriptServiceMethodParameterModel> queryParams = new ArrayList<>();
    private List<TypeScriptServiceMethodParameterModel> pathParams = new ArrayList<>();
    private String bodyParameter;
    private List<String> basePathParamNames = new ArrayList<>();

}

