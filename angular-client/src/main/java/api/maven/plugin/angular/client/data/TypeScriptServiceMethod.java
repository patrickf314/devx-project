package api.maven.plugin.angular.client.data;

import lombok.Data;

import java.util.List;

@Data
public class TypeScriptServiceMethod {

    private String name;
    private TypeScriptType returnType;
    private List<TypeScriptServiceMethodParameter> parameters;
    private String httpMethod;
    private TypeScriptPath path;
    private String returnTypeWrapper;
    private String returnTypeMapper;
    private String options;
    private boolean formData;
    private List<TypeScriptServiceMethodParameter> headerParams;
    private List<TypeScriptServiceMethodParameter> queryParams;
    private List<TypeScriptServiceMethodParameter> pathParams;
    private String bodyParameter;
    private List<String> basePathParamNames;

}

